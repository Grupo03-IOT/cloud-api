package com.pe.cloudapi.insights.application.internal.outboundservices.acl;

import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.insights.domain.ports.out.RoomCatalog;
import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.interfaces.acl.MonitoringContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code insights} y {@code monitoring}.
 *
 * <p>Traduce sus lecturas a {@link ReadingPoint} y sus salas a
 * {@link AnalyzedRoom}, el vocabulario de aquí.
 */
@Service
@RequiredArgsConstructor
public class ExternalMonitoringService implements ReadingSeriesProvider, RoomCatalog {

    private final MonitoringContextFacade monitoring;

    @Override
    public List<ReadingPoint> seriesOf(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return monitoring.readingsInRange(roomId, from, to)
                .readings().stream()
                .filter(reading -> reading.getTs().isBefore(to))
                .map(reading -> new ReadingPoint(
                        reading.getTs(),
                        reading.getAcoustic().laeq(),
                        reading.getAcoustic().backgroundNoise(),
                        reading.getClimate().tempC(),
                        reading.getComfort().ppd(),
                        reading.getOccupancy().occupiedPct()))
                .toList();
    }

    @Override
    public AnalyzedRoom room(UUID roomId) {
        return toAnalyzedRoom(monitoring.room(roomId));
    }

    @Override
    public List<AnalyzedRoom> roomsOfSite(UUID siteId) {
        return monitoring.roomsOfSite(siteId).stream()
                .map(this::toAnalyzedRoom)
                .toList();
    }

    private AnalyzedRoom toAnalyzedRoom(RoomContext context) {
        return new AnalyzedRoom(
                context.room().getId(),
                context.room().getCode(),
                context.room().getRoomTypeId(),
                context.roomType().map(RoomType::getCode).orElse(null),
                ZoneId.of(context.site().getTimezone()));
    }
}
