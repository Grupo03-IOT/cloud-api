package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;
import com.pe.cloudapi.monitoring.domain.ports.out.GatewayRepository;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.GatewayEntity;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.GatewayMapper;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.repositories.GatewayJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador del puerto {@link GatewayRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class GatewayRepositoryImpl implements GatewayRepository {

    private final GatewayJpaRepository jpa;
    private final GatewayMapper mapper;

    @Override
    public Gateway save(Gateway gateway) {
        GatewayEntity entity = gateway.getId() == null
                ? new GatewayEntity()
                : jpa.findById(gateway.getId()).orElseGet(GatewayEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, gateway)));
    }

    @Override
    public Optional<Gateway> findByCode(String code) {
        return jpa.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Gateway> findAll() {
        return jpa.findAllAlive().stream().map(mapper::toDomain).toList();
    }
}
