package com.pe.cloudapi.insights.application.internal.usecases;

import com.pe.cloudapi.insights.application.internal.ports.in.AnalyzeRoom;
import com.pe.cloudapi.insights.domain.model.errors.InsightsError;
import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.domain.model.results.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.insights.domain.services.ComfortAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Analizar una sala en un periodo.
 *
 * <p>Rechaza los rangos con muy pocos datos en vez de devolver coeficientes
 * calculados sobre cuatro puntos: un número sin respaldo se ve igual de creíble
 * que uno bueno, y eso es peor que no dar ninguno.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeRoomUseCase implements AnalyzeRoom {

    private final ReadingSeriesProvider readings;
    private final ComfortAnalyticsService analytics;

    @Override
    public RoomAnalytics execute(AnalyzeRoomQuery query) {
        List<ReadingPoint> series = readings.seriesOf(query.roomId(), query.from(), query.to());
        if (series.size() < ComfortAnalyticsService.MINIMUM_SAMPLE) {
            throw InsightsError.RANGE_TOO_SHORT.with(ComfortAnalyticsService.MINIMUM_SAMPLE);
        }
        return analytics.analyze(query.roomId(), query.from(), query.to(), series);
    }
}
