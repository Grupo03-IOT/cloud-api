package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetRoom;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.repositories.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Obtener una sala concreta con su última lectura.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetRoomUseCase implements GetRoom {

    private final RoomRepository rooms;
    private final RoomReadingRepository readings;

    /**
     * @param query sala buscada
     * @return la sala con su última lectura
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    @Override
    public RoomSnapshot execute(GetRoomQuery query) {
        Room room = rooms.findById(query.roomId())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(query.roomId()));
        return new RoomSnapshot(room, readings.findLatest(query.roomId()));
    }
}
