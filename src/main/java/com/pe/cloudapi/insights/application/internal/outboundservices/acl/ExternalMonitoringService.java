package com.pe.cloudapi.insights.application.internal.outboundservices.acl;

import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.monitoring.interfaces.acl.MonitoringContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code insights} y {@code monitoring}.
 *
 * <p>Único punto del contexto que conoce al otro: traduce sus lecturas a
 * {@link ReadingPoint}, el vocabulario de aquí.
 */
@Service
@RequiredArgsConstructor
public class ExternalMonitoringService implements ReadingSeriesProvider {

    private final MonitoringContextFacade monitoring;

    @Override
    public List<ReadingPoint> seriesOf(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return monitoring.readingsInRange(roomId, from, to)
                .readings().stream()
                .map(reading -> new ReadingPoint(
                        reading.getTs(),
                        reading.getAcoustic().laeq(),
                        reading.getAcoustic().backgroundNoise(),
                        reading.getClimate().tempC(),
                        reading.getComfort().ppd(),
                        reading.getOccupancy().occupiedPct()))
                .toList();
    }
}
