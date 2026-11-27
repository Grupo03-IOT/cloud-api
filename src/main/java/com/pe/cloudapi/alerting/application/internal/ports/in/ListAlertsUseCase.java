package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;

import java.util.List;

/**
 * Puerto de entrada: listar alertas, de la más reciente a la más antigua.
 */
public interface ListAlertsUseCase {

    List<RoomAlert> execute(ListAlertsQuery query);
}
