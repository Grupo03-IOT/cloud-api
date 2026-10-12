package com.pe.cloudapi.insights.application.internal.usecases;

import com.pe.cloudapi.insights.application.internal.ports.in.RankSiteRoomsUseCase;
import com.pe.cloudapi.insights.domain.model.queries.RankSiteRoomsQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;
import com.pe.cloudapi.insights.domain.model.valueobjects.PeriodWindow;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomRanking;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.insights.domain.ports.out.RoomCatalog;
import com.pe.cloudapi.insights.domain.ports.out.ThresholdProvider;
import com.pe.cloudapi.insights.domain.services.ComfortAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Ordenar las salas de un local por cumplimiento en un periodo.
 *
 * <p>Pide una serie por sala: es la consulta más cara de {@code insights}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankSiteRoomsUseCaseImpl implements RankSiteRoomsUseCase {

    private final RoomCatalog rooms;
    private final ReadingSeriesProvider readings;
    private final ThresholdProvider thresholds;
    private final ComfortAnalyticsService analytics;

    @Override
    public List<RoomRanking> execute(RankSiteRoomsQuery query) {
        Map<UUID, Optional<Float>> thresholdByType = new HashMap<>();
        List<RoomRanking> rankings = rooms.roomsOfSite(query.siteId()).stream()
                .map(room -> rankingOf(room, query, thresholdByType))
                .toList();
        return analytics.rank(rankings);
    }

    private RoomRanking rankingOf(AnalyzedRoom room, RankSiteRoomsQuery query,
                                  Map<UUID, Optional<Float>> thresholdByType) {
        PeriodWindow window = query.period().in(room.zone());
        List<ReadingPoint> series = readings.seriesOf(room.id(), window.from(), window.to());
        Float noiseThreshold = room.isClassified()
                ? thresholdByType.computeIfAbsent(room.roomTypeId(), thresholds::noiseWarnLevel).orElse(null)
                : null;
        return new RoomRanking(room, analytics.dataQuality(window, series),
                analytics.noiseDistribution(series, noiseThreshold));
    }
}
