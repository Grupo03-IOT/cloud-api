package com.pe.cloudapi.alerting.domain.model.aggregates;

import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertClosure;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Una superación sostenida de un umbral en una sala, desde que el Edge la
 * detecta hasta que se normaliza.
 *
 * <p>Aquí viven las reglas de estado: solo se reconoce una alerta abierta,
 * cerrar dos veces no cambia nada y una apertura repetida tampoco. El Edge
 * reintenta cuando no confirma la subida, así que cada evento puede llegar más
 * de una vez.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Alert {

    public static final int CORRECTIVE_ACTION_MIN = 3;
    public static final int CORRECTIVE_ACTION_MAX = 500;

    private final UUID id;
    private final String sourceKey;
    private final UUID roomId;
    private final String rule;
    private final AlertSeverity severity;
    private final String message;
    private final Double value;
    private final OffsetDateTime openedAt;
    private AlertStatus status;
    private OffsetDateTime closedAt;
    private AlertClosure closedBy;
    private OffsetDateTime acknowledgedAt;
    private UUID acknowledgedBy;
    private String correctiveAction;

    /**
     * Abre la alerta con el primer evento que llega del Edge. Si ese evento
     * ya trae el cierre (se abrió y se cerró sin conexión), nace cerrada.
     *
     * @param command evento recibido
     * @param roomId  id real de la sala, ya resuelto desde su código
     */
    public Alert(RecordAlertEventCommand command, UUID roomId) {
        this.id = null;
        this.sourceKey = command.sourceKey();
        this.roomId = roomId;
        this.rule = command.rule();
        this.severity = command.severity();
        this.message = command.message();
        this.value = command.value();
        this.openedAt = command.openedAt();
        this.status = AlertStatus.OPEN;
        if (command.closedAt() != null) {
            closeAutomatically(command.closedAt());
        }
    }

    /**
     * Aplica un evento repetido con el mismo {@code sourceKey}.
     *
     * <p>Lo único que puede aportar es el cierre: una apertura repetida no
     * cambia nada.
     *
     * @param command evento recibido
     */
    public void handle(RecordAlertEventCommand command) {
        if (command.closedAt() != null) {
            closeAutomatically(command.closedAt());
        }
    }

    /**
     * Reconoce la alerta dejando registrada la acción correctiva.
     *
     * @param command quién y qué acción
     * @param at      momento del reconocimiento
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_CORRECTIVE_ACTION_REQUIRED} si falta la acción o no
     *         mide de 3 a 500 caracteres; {@code ALERTING_ALERT_NOT_OPEN} si ya
     *         estaba reconocida o cerrada
     */
    public void acknowledge(AcknowledgeAlertCommand command, OffsetDateTime at) {
        String action = command.correctiveAction() == null ? null : command.correctiveAction().strip();
        if (action == null || action.length() < CORRECTIVE_ACTION_MIN
                || action.length() > CORRECTIVE_ACTION_MAX) {
            throw AlertingError.CORRECTIVE_ACTION_REQUIRED.with();
        }
        if (status != AlertStatus.OPEN) {
            throw AlertingError.ALERT_NOT_OPEN.with(id, status);
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedAt = at;
        this.acknowledgedBy = command.userId();
        this.correctiveAction = action;
    }

    /**
     * Cierra la alerta porque la condición se normalizó, esté o no reconocida.
     *
     * <p>Es idempotente: si ya estaba cerrada se conserva el primer cierre.
     *
     * @param at cuándo se normalizó, según el Edge
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_ALERT_CLOSED_BEFORE_OPENED} si el cierre es anterior a la apertura
     */
    public void closeAutomatically(OffsetDateTime at) {
        if (status == AlertStatus.CLOSED) {
            return;
        }
        if (at.isBefore(openedAt)) {
            throw AlertingError.ALERT_CLOSED_BEFORE_OPENED.with(sourceKey, at, openedAt);
        }
        this.status = AlertStatus.CLOSED;
        this.closedAt = at;
        this.closedBy = AlertClosure.AUTO;
    }
}
