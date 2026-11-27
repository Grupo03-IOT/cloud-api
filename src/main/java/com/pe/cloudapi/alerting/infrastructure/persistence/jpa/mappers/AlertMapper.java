package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertClosure;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities.AlertEntity;

import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code Alert} y su entidad JPA, convirtiendo los
 * enums a texto y de vuelta.
 */
@Component
public class AlertMapper {

    public Alert toDomain(AlertEntity entity) {
        if (entity == null) return null;
        return Alert.builder()
                .id(entity.getId())
                .sourceKey(entity.getSourceKey())
                .roomId(entity.getRoomId())
                .rule(entity.getRule())
                .severity(AlertSeverity.fromCode(entity.getSeverity()))
                .message(entity.getMessage())
                .value(entity.getValue())
                .status(AlertStatus.fromCode(entity.getStatus()))
                .openedAt(entity.getOpenedAt())
                .closedAt(entity.getClosedAt())
                .closedBy(entity.getClosedBy() == null ? null : AlertClosure.valueOf(entity.getClosedBy()))
                .acknowledgedAt(entity.getAcknowledgedAt())
                .acknowledgedBy(entity.getAcknowledgedBy())
                .correctiveAction(entity.getCorrectiveAction())
                .build();
    }

    public AlertEntity applyTo(AlertEntity entity, Alert domain) {
        entity.setSourceKey(domain.getSourceKey());
        entity.setRoomId(domain.getRoomId());
        entity.setRule(domain.getRule());
        entity.setSeverity(domain.getSeverity().toCode());
        entity.setMessage(domain.getMessage());
        entity.setValue(domain.getValue());
        entity.setStatus(domain.getStatus().name());
        entity.setOpenedAt(domain.getOpenedAt());
        entity.setClosedAt(domain.getClosedAt());
        entity.setClosedBy(domain.getClosedBy() == null ? null : domain.getClosedBy().name());
        entity.setAcknowledgedAt(domain.getAcknowledgedAt());
        entity.setAcknowledgedBy(domain.getAcknowledgedBy());
        entity.setCorrectiveAction(domain.getCorrectiveAction());
        return entity;
    }
}
