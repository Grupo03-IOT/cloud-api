package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetLatestReading;
import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.GetLatestReadingQuery;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.repositories.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Obtener la lectura más reciente de una sala.
 *
 * <p>Distingue dos ausencias que se parecen pero no son lo mismo: la sala no
 * existe, o existe pero todavía no ha reportado nada. La segunda ocurre entre
 * que se da de alta automáticamente y llega su primer minuto, y el cliente
 * necesita distinguirlas para pintar un error o un estado vacío.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GetLatestReadingUseCase implements GetLatestReading {

    private final RoomRepository rooms;
    private final RoomReadingRepository readings;

    /**
     * @param query sala consultada
     * @return la sala con su única lectura más reciente
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe, o
     *         {@code MONITORING_ROOM_HAS_NO_READINGS} si aún no ha reportado
     */
    @Override
    public RoomReadings execute(GetLatestReadingQuery query) {
        UUID roomId = query.roomId();
        Room room = rooms.findById(roomId)
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(roomId));
        RoomReading latest = readings.findLatest(roomId)
                .orElseThrow(() -> MonitoringError.ROOM_HAS_NO_READINGS.with(roomId));
        return new RoomReadings(room, List.of(latest));
    }
}
