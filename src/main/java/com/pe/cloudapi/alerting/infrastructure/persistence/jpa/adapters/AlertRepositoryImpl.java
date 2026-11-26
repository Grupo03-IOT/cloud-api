package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities.AlertEntity;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.mappers.AlertMapper;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.repositories.AlertJpaRepository;

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
 * Adaptador del puerto {@link AlertRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class AlertRepositoryImpl implements AlertRepository {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "openedAt");

    private final AlertJpaRepository jpa;
    private final AlertMapper mapper;

    @Override
    public Alert save(Alert alert) {
        AlertEntity entity = alert.getId() == null
                ? new AlertEntity()
                : jpa.findById(alert.getId()).orElseGet(AlertEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, alert)));
    }

    @Override
    public Optional<Alert> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Alert> findBySourceKey(String sourceKey) {
        return jpa.findBySourceKey(sourceKey).map(mapper::toDomain);
    }

    @Override
    public List<Alert> search(ListAlertsQuery query) {
        return jpa.findAll(matching(query), NEWEST_FIRST).stream().map(mapper::toDomain).toList();
    }

    private static Specification<AlertEntity> matching(ListAlertsQuery query) {
        return (root, criteria, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (query.status() != null) {
                filters.add(cb.equal(root.get("status"), query.status().name()));
            }
            if (query.roomId() != null) {
                filters.add(cb.equal(root.get("roomId"), query.roomId()));
            }
            if (query.from() != null) {
                filters.add(cb.greaterThanOrEqualTo(root.get("openedAt"), query.from()));
            }
            if (query.to() != null) {
                filters.add(cb.lessThanOrEqualTo(root.get("openedAt"), query.to()));
            }
            if (query.hasAction() != null) {
                filters.add(query.hasAction()
                        ? cb.isNotNull(root.get("correctiveAction"))
                        : cb.isNull(root.get("correctiveAction")));
            }
            return cb.and(filters.toArray(Predicate[]::new));
        };
    }
}
