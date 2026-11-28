package com.pe.cloudapi.alerting.interfaces.rest.transform;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AcknowledgeAlertResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AlertEventBatchResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AlertResource;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Traduce entre los recursos REST de alertas y el dominio.
 */
@Component
public class AlertResourceAssembler {

    /**
     * El código de local del sobre se copia a cada evento: el caso de uso
     * resuelve la sala de cada uno sin conocer el formato del lote.
     */
    public List<RecordAlertEventCommand> toCommands(AlertEventBatchResource batch) {
        return batch.events().stream()
                .map(event -> new RecordAlertEventCommand(
                        event.sourceKey().strip(),
                        batch.siteCode(),
                        event.roomId().strip(),
                        event.rule(),
                        AlertSeverity.fromCode(event.severity()),
                        event.message(),
                        event.value(),
                        event.openedAt(),
                        event.closedAt()))
                .toList();
    }

    public ListAlertsQuery toQuery(String status, UUID roomId, OffsetDateTime from,
                                   OffsetDateTime to, Boolean hasAction) {
        return new ListAlertsQuery(
                status == null || status.isBlank() ? null : AlertStatus.fromCode(status),
                roomId, from, to, hasAction);
    }

    public AcknowledgeAlertCommand toCommand(UUID alertId, UUID userId,
                                             AcknowledgeAlertResource resource) {
        return new AcknowledgeAlertCommand(alertId, userId, resource.correctiveAction());
    }

    public AlertResource toResource(RoomAlert result) {
        Alert alert = result.alert();
        return new AlertResource(
                alert.getId(),
                alert.getRoomId(),
                result.roomCode(),
                alert.getRule(),
                alert.getSeverity().toCode(),
                alert.getMessage(),
                alert.getValue(),
                alert.getStatus().name(),
                alert.getOpenedAt(),
                alert.getClosedAt(),
                alert.getClosedBy() == null ? null : alert.getClosedBy().name(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                result.acknowledgedByName(),
                alert.getCorrectiveAction());
    }
}
