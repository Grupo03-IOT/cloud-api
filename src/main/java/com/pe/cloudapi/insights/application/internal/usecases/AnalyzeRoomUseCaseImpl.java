package com.pe.cloudapi.insights.application.internal.usecases;

import com.pe.cloudapi.insights.application.internal.ports.in.AnalyzeRoomUseCase;
import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;
import com.pe.cloudapi.insights.domain.model.valueobjects.PeriodWindow;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.insights.domain.ports.out.RoomCatalog;
import com.pe.cloudapi.insights.domain.ports.out.ThresholdProvider;
import com.pe.cloudapi.insights.domain.ports.out.WeatherObservationRepository;
import com.pe.cloudapi.insights.domain.services.ComfortAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Analizar una sala en un periodo y compararlo con el anterior.
 *
 * <p>El periodo se resuelve en la zona horaria del local de la sala.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeRoomUseCaseImpl implements AnalyzeRoomUseCase {

    private final RoomCatalog rooms;
    private final ReadingSeriesProvider readings;
    private final WeatherObservationRepository weatherObservations;
    private final ThresholdProvider thresholds;
    private final ComfortAnalyticsService analytics;

    @Override
    public RoomAnalytics execute(AnalyzeRoomQuery query) {
        AnalyzedRoom room = rooms.room(query.roomId());
        PeriodWindow window = query.period().in(room.zone());
        PeriodWindow previous = query.period().previous().in(room.zone());
        Float noiseThreshold = room.isClassified()
                ? thresholds.noiseWarnLevel(room.roomTypeId()).orElse(null)
                : null;

        RoomAnalytics current = analytics.analyze(room.id(), window,
                readings.seriesOf(room.id(), window.from(), window.to()),
                weatherObservations.findInRange(window.from(), window.to()),
                noiseThreshold);
        return current.withComparison(analytics.compare(current.noiseDistribution(), previous.period(),
                readings.seriesOf(room.id(), previous.from(), previous.to()), noiseThreshold));
    }
}
