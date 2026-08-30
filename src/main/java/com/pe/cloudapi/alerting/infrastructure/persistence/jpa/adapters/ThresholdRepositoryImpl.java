package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.alerting.domain.ports.out.ThresholdRepository;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities.ThresholdEntity;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.mappers.ThresholdMapper;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.repositories.ThresholdJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador del puerto {@link ThresholdRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class ThresholdRepositoryImpl implements ThresholdRepository {

    private final ThresholdJpaRepository jpa;
    private final ThresholdMapper mapper;

    @Override
    public Threshold save(Threshold threshold) {
        ThresholdEntity entity = threshold.getId() == null
                ? new ThresholdEntity()
                : jpa.findById(threshold.getId()).orElseGet(ThresholdEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, threshold)));
    }

    @Override
    public List<Threshold> findEnabledByRoomTypeId(UUID roomTypeId) {
        return jpa.findEnabledByRoomTypeId(roomTypeId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Threshold> findByRoomTypeIdAndMetric(UUID roomTypeId, ThresholdMetric metric) {
        return jpa.findByRoomTypeIdAndMetric(roomTypeId, metric.toCode()).map(mapper::toDomain);
    }
}
