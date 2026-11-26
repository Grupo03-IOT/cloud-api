package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;

/**
 * Puerto de entrada: reconocer una alerta abierta con su acción correctiva.
 */
public interface AcknowledgeAlertUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_ALERT_NOT_FOUND}, {@code ALERTING_ALERT_NOT_OPEN} o
     *         {@code ALERTING_CORRECTIVE_ACTION_REQUIRED}
     */
    RoomAlert execute(AcknowledgeAlertCommand command);
}
