package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.RecordAlertEventsUseCase;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Guardar los eventos de alerta que sube el Edge.
 *
 * <p>Primero se resuelven todas las salas y solo después se escribe: un lote
 * con una sala desconocida se rechaza entero, de modo que el Edge pueda marcar
 * el evento como fallido sin que la mitad del lote haya quedado guardada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordAlertEventsUseCaseImpl implements RecordAlertEventsUseCase {

    private final AlertRepository alerts;
    private final RoomDirectory roomDirectory;

    @Override
    @Transactional
    public int execute(List<RecordAlertEventCommand> events) {
        if (events == null || events.isEmpty()) {
            throw AlertingError.ALERT_BATCH_EMPTY.with();
        }
        Map<String, UUID> roomIds = new HashMap<>();
        for (RecordAlertEventCommand event : events) {
            roomIds.computeIfAbsent(roomKey(event),
                    key -> roomDirectory.roomIdOf(event.siteCode(), event.roomCode()));
        }
        for (RecordAlertEventCommand event : events) {
            UUID roomId = roomIds.get(roomKey(event));
            Alert alert = alerts.findBySourceKey(event.sourceKey())
                    .map(existing -> {
                        existing.handle(event);
                        return existing;
                    })
                    .orElseGet(() -> new Alert(event, roomId));
            Alert saved = alerts.save(alert);
            log.info("alert.recorded sourceKey={} room={} status={}",
                    saved.getSourceKey(), saved.getRoomId(), saved.getStatus());
        }
        return events.size();
    }

    private static String roomKey(RecordAlertEventCommand event) {
        return (event.siteCode() == null ? "" : event.siteCode()) + "/" + event.roomCode();
    }
}
