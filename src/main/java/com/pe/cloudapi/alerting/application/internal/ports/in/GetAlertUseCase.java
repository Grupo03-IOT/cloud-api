package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.queries.GetAlertQuery;

/**
 * Puerto de entrada: una alerta concreta.
 */
public interface GetAlertUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code ALERTING_ALERT_NOT_FOUND} si no existe
     */
    RoomAlert execute(GetAlertQuery query);
}
