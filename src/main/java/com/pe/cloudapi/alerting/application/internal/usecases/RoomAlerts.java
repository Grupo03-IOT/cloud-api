package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;
import com.pe.cloudapi.alerting.domain.ports.out.UserDirectory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Completa las alertas con lo que muestran las apps: el código de la sala y el
 * nombre de quien las reconoció. Consulta cada directorio una sola vez, sean
 * cuantas sean las alertas.
 */
@Component
@RequiredArgsConstructor
class RoomAlerts {

    private final RoomDirectory roomDirectory;
    private final UserDirectory userDirectory;

    List<RoomAlert> of(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return List.of();
        }
        Map<UUID, String> codes = roomDirectory.roomCodes();
        Set<UUID> acknowledgers = alerts.stream()
                .map(Alert::getAcknowledgedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> names = acknowledgers.isEmpty() ? Map.of() : userDirectory.namesOf(acknowledgers);
        return alerts.stream()
                .map(alert -> new RoomAlert(alert, codes.get(alert.getRoomId()),
                        alert.getAcknowledgedBy() == null ? null : names.get(alert.getAcknowledgedBy())))
                .toList();
    }

    RoomAlert of(Alert alert) {
        return of(List.of(alert)).getFirst();
    }
}
