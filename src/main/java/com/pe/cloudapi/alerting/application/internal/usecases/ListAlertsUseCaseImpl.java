package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.ListAlertsUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Listar alertas con el código actual de su sala.
 *
 * <p>Sirve a la vez para las activas, las reconocidas y el historial (US36):
 * cambia solo el filtro de estado.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListAlertsUseCaseImpl implements ListAlertsUseCase {

    private final AlertRepository alerts;
    private final RoomDirectory roomDirectory;

    @Override
    public List<RoomAlert> execute(ListAlertsQuery query) {
        List<Alert> found = alerts.search(query);
        if (found.isEmpty()) {
            return List.of();
        }
        Map<UUID, String> codes = roomDirectory.roomCodes();
        return found.stream()
                .map(alert -> new RoomAlert(alert, codes.get(alert.getRoomId())))
                .toList();
    }
}
