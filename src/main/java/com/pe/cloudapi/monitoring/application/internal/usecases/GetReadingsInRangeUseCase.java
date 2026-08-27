package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetReadingsInRange;
import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.GetReadingsInRangeQuery;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.repositories.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Obtener la serie temporal de una sala entre dos instantes.
 *
 * <p>Que la sala exista se comprueba aquí y no en el controlador: es una regla
 * de la operación, no del transporte. Si el rango está invertido, lo rechaza el
 * propio {@link GetReadingsInRangeQuery} al construirse.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GetReadingsInRangeUseCase implements GetReadingsInRange {

    private final RoomRepository rooms;
    private final RoomReadingRepository readings;

    /**
     * @param query sala y rango consultados
     * @return la sala con sus lecturas del periodo
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si la sala no existe
     */
    @Override
    public RoomReadings execute(GetReadingsInRangeQuery query) {
        Room room = rooms.findById(query.roomId())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(query.roomId()));
        return new RoomReadings(room,
                readings.findInRange(query.roomId(), query.from(), query.to()));
    }
}
