package com.pe.cloudapi.alerting.domain.model.commands;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;

import java.time.OffsetDateTime;

/**
 * Un evento de alerta tal como lo sube el Edge: la apertura y, si ya ocurrió,
 * el cierre.
 *
 * @param sourceKey código del Edge + {@code :} + id local; clave de idempotencia
 * @param siteCode  local al que pertenece la sala; opcional si solo hay uno
 * @param roomCode  código de sala que maneja el Edge ({@code sala-03})
 * @param rule      regla que se incumplió ({@code sustained_noise})
 * @param severity  gravedad declarada por el Edge
 * @param message   texto legible de la alerta
 * @param value     valor medido que la disparó; puede faltar
 * @param openedAt  cuándo empezó la condición
 * @param closedAt  cuándo se normalizó; {@code null} si sigue abierta
 */
public record RecordAlertEventCommand(
        String sourceKey,
        String siteCode,
        String roomCode,
        String rule,
        AlertSeverity severity,
        String message,
        Double value,
        OffsetDateTime openedAt,
        OffsetDateTime closedAt
) {

    public RecordAlertEventCommand {
        if (closedAt != null && openedAt != null && closedAt.isBefore(openedAt)) {
            throw AlertingError.ALERT_CLOSED_BEFORE_OPENED.with(sourceKey, closedAt, openedAt);
        }
    }
}
