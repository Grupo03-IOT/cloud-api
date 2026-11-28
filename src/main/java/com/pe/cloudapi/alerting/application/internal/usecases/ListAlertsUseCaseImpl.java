package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.ListAlertsUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar alertas con su sala y quién las reconoció.
 *
 * <p>Sirve a la vez para las activas, las reconocidas y el historial (US36):
 * cambia solo el filtro de estado.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListAlertsUseCaseImpl implements ListAlertsUseCase {

    private final AlertRepository alerts;
    private final RoomAlerts roomAlerts;

    @Override
    public List<RoomAlert> execute(ListAlertsQuery query) {
        return roomAlerts.of(alerts.search(query));
    }
}
