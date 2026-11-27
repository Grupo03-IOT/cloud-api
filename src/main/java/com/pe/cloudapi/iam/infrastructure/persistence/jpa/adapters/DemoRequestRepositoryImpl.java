package com.pe.cloudapi.iam.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.ports.out.DemoRequestRepository;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers.DemoRequestMapper;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories.DemoRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador del puerto {@link DemoRequestRepository} sobre Spring Data.
 *
 * <p>Una solicitud no se edita: {@link #save} siempre inserta.
 */
@Component
@RequiredArgsConstructor
public class DemoRequestRepositoryImpl implements DemoRequestRepository {

    private final DemoRequestJpaRepository jpa;
    private final DemoRequestMapper mapper;

    @Override
    public DemoRequest save(DemoRequest request) {
        return mapper.toDomain(jpa.save(mapper.toEntity(request)));
    }

    @Override
    public List<DemoRequest> findAllNewestFirst() {
        return jpa.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }
}
