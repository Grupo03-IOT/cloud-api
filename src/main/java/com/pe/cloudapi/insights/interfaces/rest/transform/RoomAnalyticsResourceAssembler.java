package com.pe.cloudapi.insights.interfaces.rest.transform;

import com.pe.cloudapi.insights.domain.model.valueobjects.Comparison;
import com.pe.cloudapi.insights.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.insights.domain.model.valueobjects.Direction;
import com.pe.cloudapi.insights.domain.model.valueobjects.Finding;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourSlot;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourlyProfile;
import com.pe.cloudapi.insights.domain.model.valueobjects.Measure;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseDistribution;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisode;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisodes;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomRanking;
import com.pe.cloudapi.insights.interfaces.rest.resources.HourlyProfileResource;
import com.pe.cloudapi.insights.interfaces.rest.resources.RoomAnalyticsResource;
import com.pe.cloudapi.insights.interfaces.rest.resources.SiteRoomsRankingResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Traduce las conclusiones del dominio a su representación REST.
 */
@Component
public class RoomAnalyticsResourceAssembler {

    public RoomAnalyticsResource toResource(RoomAnalytics analytics) {
        return new RoomAnalyticsResource(
                analytics.roomId(),
                analytics.window().period().label(),
                analytics.window().from(),
                analytics.window().to(),
                analytics.window().zone().getId(),
                toResource(analytics.dataQuality()),
                analytics.findings().stream().map(this::toResource).toList(),
                new RoomAnalyticsResource.MeasuresResource(
                        toResource(analytics.noiseVsOccupancy()),
                        toResource(analytics.thermalDrift()),
                        toResource(analytics.indoorVsOutdoor())),
                toResource(analytics.noiseEpisodes()),
                toResource(analytics.noiseDistribution()),
                toResource(analytics.comparison()));
    }

    public HourlyProfileResource toResource(HourlyProfile profile) {
        return new HourlyProfileResource(
                profile.roomId(),
                profile.window().period().label(),
                profile.window().from(),
                profile.window().to(),
                profile.window().zone().getId(),
                profile.hours().stream().map(this::toResource).toList());
    }

    public SiteRoomsRankingResource toResource(UUID siteId, Period period, List<RoomRanking> rankings) {
        return new SiteRoomsRankingResource(siteId, period.label(),
                rankings.stream().map(this::toResource).toList());
    }

    private SiteRoomsRankingResource.RoomRankingResource toResource(RoomRanking ranking) {
        return new SiteRoomsRankingResource.RoomRankingResource(
                ranking.room().id(),
                ranking.room().code(),
                ranking.room().roomTypeCode(),
                toResource(ranking.dataQuality()),
                toResource(ranking.noiseDistribution()));
    }

    private RoomAnalyticsResource.DataQualityResource toResource(DataQuality quality) {
        return new RoomAnalyticsResource.DataQualityResource(
                quality.expectedMinutes(), quality.receivedMinutes(), quality.coveragePct());
    }

    private RoomAnalyticsResource.FindingResource toResource(Finding finding) {
        return new RoomAnalyticsResource.FindingResource(
                finding.code(), finding.value(), finding.unit(), codeOf(finding.direction()));
    }

    private RoomAnalyticsResource.MeasureResource toResource(Measure measure) {
        return new RoomAnalyticsResource.MeasureResource(
                measure.verdict().toCode(),
                measure.code(),
                measure.value(),
                measure.unit(),
                codeOf(measure.direction()),
                new RoomAnalyticsResource.ConfidenceResource(measure.sampleSize(), measure.rSquared()));
    }

    private RoomAnalyticsResource.NoiseEpisodesResource toResource(NoiseEpisodes episodes) {
        return new RoomAnalyticsResource.NoiseEpisodesResource(
                episodes.total(), episodes.worst().stream().map(this::toResource).toList());
    }

    private RoomAnalyticsResource.NoiseEpisodeResource toResource(NoiseEpisode episode) {
        return new RoomAnalyticsResource.NoiseEpisodeResource(
                episode.from(), episode.to(), episode.minutes(), episode.peakLaeq(), episode.peakAt());
    }

    private RoomAnalyticsResource.NoiseDistributionResource toResource(NoiseDistribution distribution) {
        if (distribution == null) {
            return null;
        }
        return new RoomAnalyticsResource.NoiseDistributionResource(
                distribution.p50(), distribution.p90(), distribution.p95(), distribution.sampleSize(),
                distribution.threshold(), distribution.minutesOver(), distribution.pctOver());
    }

    private RoomAnalyticsResource.ComparisonResource toResource(Comparison comparison) {
        if (comparison == null) {
            return null;
        }
        return new RoomAnalyticsResource.ComparisonResource(
                comparison.previousPeriod(), comparison.pctOverDelta(), comparison.p90Delta());
    }

    private HourlyProfileResource.HourSlotResource toResource(HourSlot slot) {
        return new HourlyProfileResource.HourSlotResource(
                slot.hour(), slot.verdict().toCode(), slot.sampleSize(),
                slot.avgLaeq(), slot.p90Laeq(), slot.avgOccupiedPct());
    }

    private static String codeOf(Direction direction) {
        return direction == null ? null : direction.toCode();
    }
}
