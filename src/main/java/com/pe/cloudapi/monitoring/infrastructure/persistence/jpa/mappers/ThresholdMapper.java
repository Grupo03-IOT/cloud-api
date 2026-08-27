package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.entities.Threshold;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.ThresholdEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre la entidad de dominio {@code Threshold} y su entidad JPA,
 * convirtiendo el enum {@code ThresholdMetric} a texto y de vuelta.
 */
@Component
public class ThresholdMapper {

    public Threshold toDomain(ThresholdEntity entity) {
        if (entity == null) return null;
        return Threshold.builder()
                .id(entity.getId())
                .roomTypeId(entity.getRoomTypeId())
                .metric(ThresholdMetric.fromCode(entity.getMetric()))
                .warnValue(entity.getWarnValue())
                .criticalValue(entity.getCriticalValue())
                .sustainedMinutes(entity.getSustainedMinutes() == null ? 2 : entity.getSustainedMinutes())
                .enabled(Boolean.TRUE.equals(entity.getEnabled()))
                .build();
    }

    public ThresholdEntity toEntity(Threshold domain) {
        return applyTo(new ThresholdEntity(), domain);
    }

    public ThresholdEntity applyTo(ThresholdEntity entity, Threshold domain) {
        entity.setRoomTypeId(domain.getRoomTypeId());
        entity.setMetric(domain.getMetric().toCode());
        entity.setWarnValue(domain.getWarnValue());
        entity.setCriticalValue(domain.getCriticalValue());
        entity.setSustainedMinutes(domain.getSustainedMinutes());
        entity.setEnabled(domain.isEnabled());
        return entity;
    }
}
