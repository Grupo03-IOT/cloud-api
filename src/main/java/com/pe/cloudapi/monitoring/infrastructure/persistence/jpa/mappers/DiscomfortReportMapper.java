package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.DiscomfortReportEntity;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@code DiscomfortReport} y su entidad JPA.
 *
 * <p>Sin {@code reading_ts} no hay lectura: es lo que distingue «no había
 * lectura reciente» de una lectura con todos sus valores nulos.
 */
@Component
public class DiscomfortReportMapper {

    public DiscomfortReport toDomain(DiscomfortReportEntity entity) {
        if (entity == null) return null;
        return DiscomfortReport.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .userId(entity.getUserId())
                .category(DiscomfortCategory.fromCode(entity.getCategory()))
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .reading(entity.getReadingTs() == null ? null : new ReadingAtReport(
                        entity.getReadingTs(), entity.getLaeq(), entity.getTempC(),
                        entity.getRhPct(), entity.getOccupiedPct()))
                .build();
    }

    public DiscomfortReportEntity toEntity(DiscomfortReport domain) {
        DiscomfortReportEntity entity = new DiscomfortReportEntity();
        entity.setRoomId(domain.getRoomId());
        entity.setUserId(domain.getUserId());
        entity.setCategory(domain.getCategory().name());
        entity.setComment(domain.getComment());
        entity.setCreatedAt(domain.getCreatedAt());
        ReadingAtReport reading = domain.getReading();
        if (reading != null) {
            entity.setReadingTs(reading.ts());
            entity.setLaeq(reading.laeq());
            entity.setTempC(reading.tempC());
            entity.setRhPct(reading.rhPct());
            entity.setOccupiedPct(reading.occupiedPct());
        }
        return entity;
    }
}
