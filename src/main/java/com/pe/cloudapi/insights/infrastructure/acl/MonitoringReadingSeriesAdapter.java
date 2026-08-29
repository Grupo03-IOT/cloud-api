package com.pe.cloudapi.insights.infrastructure.acl;

import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code insights} y {@code monitoring}.
 *
 * <p>Es el <strong>único</strong> punto de todo el contexto que conoce el
 * modelo del otro. Traduce sus lecturas a {@link ReadingPoint}, que es el
 * vocabulario propio de este contexto, y con eso el dominio de aquí queda
 * aislado: si mañana las series vinieran de otra parte —de un almacén de series
 * temporales, de un fichero— cambiaría esta clase y nada más.
 *
 * <p>Vive en infraestructura precisamente por eso: la dependencia entre
 * contextos es un detalle de implementación, no parte del modelo.
 */
@Component
@RequiredArgsConstructor
public class MonitoringReadingSeriesAdapter implements ReadingSeriesProvider {

    private final RoomReadingRepository readings;

    @Override
    public List<ReadingPoint> seriesOf(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return readings.findInRange(roomId, from, to).stream()
                .map(this::toPoint)
                .toList();
    }

    private ReadingPoint toPoint(RoomReading reading) {
        return new ReadingPoint(
                reading.getTs(),
                reading.getAcoustic().laeq(),
                reading.getAcoustic().backgroundNoise(),
                reading.getClimate().tempC(),
                reading.getComfort().ppd(),
                reading.getOccupancy().occupiedPct());
    }
}
