package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.ports.out.DiscomfortReportRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DiscomfortReportEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.DiscomfortReportMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.DiscomfortReportJpaRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link DiscomfortReportRepository} sobre Spring Data.
 *
 * <p>Un reporte no se edita: {@link #save} siempre inserta.
 */
@Component
@RequiredArgsConstructor
public class DiscomfortReportRepositoryImpl implements DiscomfortReportRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final DiscomfortReportJpaRepository jpa;
    private final DiscomfortReportMapper mapper;

    @Override
    public DiscomfortReport save(DiscomfortReport report) {
        return mapper.toDomain(jpa.save(mapper.toEntity(report)));
    }

    @Override
    public Optional<DiscomfortReport> findLatest(UUID userId, UUID roomId, DiscomfortCategory category) {
        return jpa.findFirstByUserIdAndRoomIdAndCategoryOrderByCreatedAtDesc(userId, roomId, category.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<DiscomfortReport> search(ListDiscomfortReportsQuery query) {
        return jpa.findAll(matching(query), NEWEST_FIRST).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DiscomfortReport> findByUserId(UUID userId) {
        return jpa.findByUserIdOrderByCreatedAtDesc(userId).stream().map(mapper::toDomain).toList();
    }

    private static Specification<DiscomfortReportEntity> matching(ListDiscomfortReportsQuery query) {
        return (root, criteria, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (query.roomId() != null) {
                filters.add(cb.equal(root.get("roomId"), query.roomId()));
            }
            if (query.category() != null) {
                filters.add(cb.equal(root.get("category"), query.category().name()));
            }
            if (query.from() != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.from()));
            }
            if (query.to() != null) {
                filters.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.to()));
            }
            return cb.and(filters.toArray(Predicate[]::new));
        };
    }
}
