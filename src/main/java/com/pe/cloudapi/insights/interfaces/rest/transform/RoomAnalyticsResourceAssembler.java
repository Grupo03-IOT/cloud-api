package com.pe.cloudapi.insights.interfaces.rest.transform;

import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.Correlation;
import com.pe.cloudapi.insights.domain.model.valueobjects.Trend;
import com.pe.cloudapi.insights.interfaces.rest.resources.RoomAnalyticsResource;
import org.springframework.stereotype.Component;

/**
 * Traduce las conclusiones del dominio a su representación REST.
 */
@Component
public class RoomAnalyticsResourceAssembler {

    public RoomAnalyticsResource toResource(RoomAnalytics analytics) {
        return new RoomAnalyticsResource(
                analytics.roomId(),
                analytics.from(),
                analytics.to(),
                analytics.sampleSize(),
                toResource(analytics.noiseVsOccupancy()),
                toResource(analytics.thermalDrift()),
                toResource(analytics.indoorVsOutdoor()),
                analytics.noiseAnomalies());
    }

    private RoomAnalyticsResource.CorrelationResource toResource(Correlation correlation) {
        return new RoomAnalyticsResource.CorrelationResource(
                correlation.coefficient(),
                correlation.strength(),
                correlation.sampleSize(),
                correlation.isReliable());
    }

    private RoomAnalyticsResource.TrendResource toResource(Trend trend) {
        return new RoomAnalyticsResource.TrendResource(
                trend.slopePerHour(),
                trend.rSquared(),
                trend.sampleSize(),
                trend.isReliable());
    }
}
