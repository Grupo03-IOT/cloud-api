package com.pe.cloudapi.insights.domain.services;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;
import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;
import com.pe.cloudapi.insights.domain.model.valueobjects.Comparison;
import com.pe.cloudapi.insights.domain.model.valueobjects.Correlation;
import com.pe.cloudapi.insights.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.insights.domain.model.valueobjects.Direction;
import com.pe.cloudapi.insights.domain.model.valueobjects.Finding;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourSlot;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourlyProfile;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseDistribution;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisode;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisodes;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.domain.model.valueobjects.PeriodWindow;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomRanking;
import com.pe.cloudapi.insights.domain.model.valueobjects.Verdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("Comfort analytics service")
class ComfortAnalyticsServiceTest {

    private static final OffsetDateTime START = OffsetDateTime.of(2026, 10, 12, 8, 0, 0, 0, ZoneOffset.UTC);
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final PeriodWindow DAY = Period.parse("2026-10-12").in(LIMA);
    private static final float EMPTY_ROOM_DBA = 36.4f;
    private static final float OCCUPIED_ROOM_DBA = 57.5f;

    private final ComfortAnalyticsService service = new ComfortAnalyticsService();

    @Nested
    @DisplayName("noise peaks")
    class NoisePeaks {

        @Test
        @DisplayName("are none when the room only moves between empty and occupied")
        void occupancyChangesAreNotPeaks() {
            List<ReadingPoint> series = new ArrayList<>();
            series.addAll(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.addAll(minutes(60, 60, OCCUPIED_ROOM_DBA, 100f));

            assertThat(peaksOf(series)).isEmpty();
        }

        @Test
        @DisplayName("flag a peak while the room is empty")
        void peakInEmptyRoom() {
            List<ReadingPoint> series = new ArrayList<>();
            series.addAll(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.addAll(minutes(60, 60, OCCUPIED_ROOM_DBA, 100f));
            series.set(20, point(20, EMPTY_ROOM_DBA + 15, 0f));

            assertThat(peaksOf(series)).containsExactly(START.plusMinutes(20));
        }

        @Test
        @DisplayName("flag a peak while the room is occupied")
        void peakInOccupiedRoom() {
            List<ReadingPoint> series = new ArrayList<>();
            series.addAll(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.addAll(minutes(60, 60, OCCUPIED_ROOM_DBA, 100f));
            series.set(90, point(90, OCCUPIED_ROOM_DBA + 15, 100f));

            assertThat(peaksOf(series)).containsExactly(START.plusMinutes(90));
        }

        @Test
        @DisplayName("ignore a rise too small to be heard, however steady the baseline")
        void smallRiseIsNotAPeak() {
            List<ReadingPoint> series = new ArrayList<>(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.set(30, point(30, EMPTY_ROOM_DBA + 2, 0f));

            assertThat(peaksOf(series)).isEmpty();
        }

        @Test
        @DisplayName("ignore drops below the baseline")
        void dropsAreNotPeaks() {
            List<ReadingPoint> series = new ArrayList<>(minutes(0, 60, OCCUPIED_ROOM_DBA, 100f));
            series.set(30, point(30, OCCUPIED_ROOM_DBA - 15, 100f));

            assertThat(peaksOf(series)).isEmpty();
        }

        @Test
        @DisplayName("skip a state with too few minutes to have a baseline")
        void sparseStateIsNotEvaluated() {
            List<ReadingPoint> series = new ArrayList<>();
            series.addAll(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.addAll(minutes(60, 10, OCCUPIED_ROOM_DBA, 100f));
            series.set(65, point(65, OCCUPIED_ROOM_DBA + 20, 100f));

            assertThat(peaksOf(series)).isEmpty();
        }

        @Test
        @DisplayName("come back oldest first across both states")
        void sortedAcrossStates() {
            List<ReadingPoint> series = new ArrayList<>();
            series.addAll(minutes(0, 60, OCCUPIED_ROOM_DBA, 100f));
            series.addAll(minutes(60, 60, EMPTY_ROOM_DBA, 0f));
            series.set(10, point(10, OCCUPIED_ROOM_DBA + 15, 100f));
            series.set(70, point(70, EMPTY_ROOM_DBA + 15, 0f));
            series.set(5, point(5, OCCUPIED_ROOM_DBA + 15, 100f));

            assertThat(peaksOf(series)).containsExactly(
                    START.plusMinutes(5), START.plusMinutes(10), START.plusMinutes(70));
        }

        private List<OffsetDateTime> peaksOf(List<ReadingPoint> series) {
            return service.noisePeaks(series).stream().map(ReadingPoint::ts).toList();
        }
    }

    @Nested
    @DisplayName("noise episodes")
    class NoiseEpisodesGrouping {

        @Test
        @DisplayName("merge consecutive peaks into one episode with its loudest minute")
        void consecutivePeaksAreOneEpisode() {
            List<ReadingPoint> series = new ArrayList<>(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.set(20, point(20, EMPTY_ROOM_DBA + 12, 0f));
            series.set(21, point(21, EMPTY_ROOM_DBA + 18, 0f));
            series.set(22, point(22, EMPTY_ROOM_DBA + 14, 0f));

            NoiseEpisodes episodes = service.noiseEpisodes(series);

            assertThat(episodes.total()).isEqualTo(1);
            NoiseEpisode episode = episodes.worst().getFirst();
            assertThat(episode.from()).isEqualTo(START.plusMinutes(20));
            assertThat(episode.to()).isEqualTo(START.plusMinutes(22));
            assertThat(episode.minutes()).isEqualTo(3);
            assertThat(episode.peakAt()).isEqualTo(START.plusMinutes(21));
            assertThat(episode.peakLaeq()).isCloseTo(EMPTY_ROOM_DBA + 18, within(0.01f));
        }

        @Test
        @DisplayName("are not split by a gap of two minutes, but are by a gap of three")
        void gapTolerance() {
            List<ReadingPoint> series = new ArrayList<>(minutes(0, 60, EMPTY_ROOM_DBA, 0f));
            series.set(10, point(10, EMPTY_ROOM_DBA + 15, 0f));
            series.set(13, point(13, EMPTY_ROOM_DBA + 15, 0f));
            series.set(30, point(30, EMPTY_ROOM_DBA + 15, 0f));
            series.set(34, point(34, EMPTY_ROOM_DBA + 15, 0f));

            assertThat(service.noiseEpisodes(series).total()).isEqualTo(3);
        }

        @Test
        @DisplayName("count every episode even when only the worst five are listed")
        void totalCountsAllEpisodes() {
            List<ReadingPoint> series = new ArrayList<>(minutes(0, 120, EMPTY_ROOM_DBA, 0f));
            for (int i = 0; i < 7; i++) {
                int minute = 10 + i * 10;
                series.set(minute, point(minute, EMPTY_ROOM_DBA + 10 + i, 0f));
            }

            NoiseEpisodes episodes = service.noiseEpisodes(series);

            assertThat(episodes.total()).isEqualTo(7);
            assertThat(episodes.worst()).hasSize(5);
            assertThat(episodes.worst().getFirst().peakLaeq()).isCloseTo(EMPTY_ROOM_DBA + 16, within(0.01f));
        }
    }

    @Nested
    @DisplayName("indoor versus outdoor temperature")
    class IndoorVsOutdoor {

        @Test
        @DisplayName("correlates every observation that has an indoor reading close enough")
        void pairsWithinTolerance() {
            Correlation correlation = service.indoorVsOutdoor(
                    indoorEveryMinute(0, 600), outdoorEveryTenMinutes(0, 600));

            assertThat(correlation.sampleSize()).isEqualTo(60);
            assertThat(correlation.coefficient()).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("drops the observations that fall in a gap of the indoor series")
        void dropsObservationsInsideAGap() {
            List<ReadingPoint> indoor = new ArrayList<>();
            indoor.addAll(indoorEveryMinute(0, 120));
            indoor.addAll(indoorEveryMinute(480, 120));

            Correlation correlation = service.indoorVsOutdoor(indoor, outdoorEveryTenMinutes(0, 600));

            assertThat(correlation.sampleSize()).isEqualTo(25);
            assertThat(correlation.coefficient()).isNull();
            assertThat(correlation.verdict()).isEqualTo(Verdict.NOT_ENOUGH_EVIDENCE);
        }

        @Test
        @DisplayName("counts pairs, not observations, as its sample size")
        void sampleSizeCountsPairs() {
            List<WeatherObservation> outdoor = new ArrayList<>(outdoorEveryTenMinutes(0, 600));
            outdoor.addAll(outdoorEveryTenMinutes(700, 50));

            assertThat(service.indoorVsOutdoor(indoorEveryMinute(0, 600), outdoor).sampleSize()).isEqualTo(60);
        }

        @Test
        @DisplayName("does not depend on the order the series arrive in")
        void ignoresInputOrder() {
            List<ReadingPoint> indoor = new ArrayList<>(indoorEveryMinute(0, 600));
            List<WeatherObservation> outdoor = new ArrayList<>(outdoorEveryTenMinutes(0, 600));
            Collections.reverse(indoor);
            Collections.shuffle(outdoor, new Random(7));

            Correlation correlation = service.indoorVsOutdoor(indoor, outdoor);

            assertThat(correlation.sampleSize()).isEqualTo(60);
            assertThat(correlation.coefficient()).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("has no data without indoor temperature")
        void noIndoorTemperature() {
            Correlation correlation = service.indoorVsOutdoor(List.of(), outdoorEveryTenMinutes(0, 600));

            assertThat(correlation.sampleSize()).isZero();
            assertThat(correlation.verdict()).isEqualTo(Verdict.NO_DATA);
        }

        private static List<ReadingPoint> indoorEveryMinute(int fromMinute, int count) {
            List<ReadingPoint> points = new ArrayList<>();
            for (int minute = fromMinute; minute < fromMinute + count; minute++) {
                points.add(new ReadingPoint(START.plusMinutes(minute), 40f, null,
                        20f + minute * 0.005f, 6f, 0f));
            }
            return points;
        }

        private static List<WeatherObservation> outdoorEveryTenMinutes(int fromMinute, int span) {
            List<WeatherObservation> observations = new ArrayList<>();
            for (int minute = fromMinute; minute < fromMinute + span; minute += 10) {
                observations.add(new WeatherObservation(START.plusMinutes(minute),
                        15f + minute * 0.01f, 70f, "Clouds"));
            }
            return observations;
        }
    }

    @Nested
    @DisplayName("verdicts and findings")
    class VerdictsAndFindings {

        @Test
        @DisplayName("withhold the slope of a drift the line does not explain")
        void weakFitHasNoValue() {
            List<ReadingPoint> series = new ArrayList<>();
            for (int minute = 0; minute < 120; minute++) {
                series.add(new ReadingPoint(START.plusMinutes(minute), 45f, null,
                        minute % 2 == 0 ? 22f : 23f, 6f, 50f));
            }

            RoomAnalytics analytics = service.analyze(UUID.randomUUID(), DAY, series, List.of(), null);

            assertThat(analytics.thermalDrift().verdict()).isEqualTo(Verdict.NOT_ENOUGH_EVIDENCE);
            assertThat(analytics.thermalDrift().value()).isNull();
            assertThat(analytics.thermalDrift().code()).isNull();
            assertThat(analytics.thermalDrift().rSquared()).isLessThan(0.5);
        }

        @Test
        @DisplayName("tell a room that is louder when empty apart, with its own code")
        void negativeCorrelationKeepsItsSign() {
            List<ReadingPoint> series = new ArrayList<>();
            for (int minute = 0; minute < 100; minute++) {
                float occupancy = minute;
                series.add(new ReadingPoint(START.plusMinutes(minute), 70f - occupancy * 0.2f + (minute % 3) * 0.1f,
                        null, null, null, occupancy));
            }

            RoomAnalytics analytics = service.analyze(UUID.randomUUID(), DAY, series, List.of(), null);

            assertThat(analytics.noiseVsOccupancy().verdict()).isEqualTo(Verdict.CONCLUSIVE);
            assertThat(analytics.noiseVsOccupancy().code()).isEqualTo("noise_without_occupancy");
            assertThat(analytics.noiseVsOccupancy().direction()).isEqualTo(Direction.NEGATIVE);
            assertThat(analytics.findings()).extracting(Finding::code).contains("noise_without_occupancy");
        }

        @Test
        @DisplayName("report a period without readings as no data, not as an error")
        void emptyPeriod() {
            RoomAnalytics analytics = service.analyze(UUID.randomUUID(), DAY, List.of(), List.of(), 55f);

            assertThat(analytics.noiseVsOccupancy().verdict()).isEqualTo(Verdict.NO_DATA);
            assertThat(analytics.thermalDrift().verdict()).isEqualTo(Verdict.NO_DATA);
            assertThat(analytics.indoorVsOutdoor().verdict()).isEqualTo(Verdict.NO_DATA);
            assertThat(analytics.dataQuality().coveragePct()).isZero();
            assertThat(analytics.noiseDistribution()).isNull();
            assertThat(analytics.noiseEpisodes().total()).isZero();
            assertThat(analytics.findings()).isEmpty();
        }

        @Test
        @DisplayName("count the time above the threshold as a finding once it is significant")
        void oftenAboveThreshold() {
            RoomAnalytics analytics = service.analyze(UUID.randomUUID(), DAY,
                    minutes(0, 60, OCCUPIED_ROOM_DBA, 100f), List.of(), 50f);

            assertThat(analytics.findings()).extracting(Finding::code).contains("often_above_threshold");
        }

        @Test
        @DisplayName("measure coverage against the whole period")
        void coverage() {
            DataQuality quality = service.dataQuality(DAY, minutes(0, 60, EMPTY_ROOM_DBA, 0f));

            assertThat(quality.expectedMinutes()).isEqualTo(1440);
            assertThat(quality.receivedMinutes()).isEqualTo(60);
            assertThat(quality.coveragePct()).isEqualTo(4.2);
        }
    }

    @Nested
    @DisplayName("noise distribution")
    class Distribution {

        @Test
        @DisplayName("gives percentiles and the time above the threshold")
        void percentilesAndCompliance() {
            NoiseDistribution distribution = service.noiseDistribution(levelsFromOneToHundred(), 80f);

            assertThat(distribution.p50()).isCloseTo(50.5, within(0.1));
            assertThat(distribution.p90()).isCloseTo(90.9, within(0.2));
            assertThat(distribution.sampleSize()).isEqualTo(100);
            assertThat(distribution.minutesOver()).isEqualTo(20);
            assertThat(distribution.pctOver()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("leaves compliance out when the room has no threshold")
        void withoutThreshold() {
            NoiseDistribution distribution = service.noiseDistribution(levelsFromOneToHundred(), null);

            assertThat(distribution.threshold()).isNull();
            assertThat(distribution.minutesOver()).isNull();
            assertThat(distribution.pctOver()).isNull();
        }

        @Test
        @DisplayName("does not exist without noise readings")
        void withoutReadings() {
            assertThat(service.noiseDistribution(List.of(), 55f)).isNull();
        }

        @Test
        @DisplayName("compares against the previous period, negative being better")
        void comparison() {
            NoiseDistribution current = service.noiseDistribution(minutes(0, 60, OCCUPIED_ROOM_DBA, 100f), 50f);

            Comparison comparison = service.compare(current, Period.parse("2026-10-11"),
                    minutes(0, 60, 40f, 100f), 50f);

            assertThat(comparison.previousPeriod()).isEqualTo("2026-10-11");
            assertThat(comparison.pctOverDelta()).isEqualTo(100.0);
            assertThat(comparison.p90Delta()).isPositive();
        }

        @Test
        @DisplayName("has no comparison when the previous period has no readings")
        void noPreviousData() {
            NoiseDistribution current = service.noiseDistribution(minutes(0, 60, OCCUPIED_ROOM_DBA, 100f), 50f);

            assertThat(service.compare(current, Period.parse("2026-10-11"), List.of(), 50f)).isNull();
        }

        private List<ReadingPoint> levelsFromOneToHundred() {
            List<ReadingPoint> points = new ArrayList<>();
            for (int level = 1; level <= 100; level++) {
                points.add(point(level, level, 100f));
            }
            return points;
        }
    }

    @Nested
    @DisplayName("hourly profile")
    class Hourly {

        @Test
        @DisplayName("averages decibels by energy, not arithmetically")
        void energyAverage() {
            assertThat(ComfortAnalyticsService.energyAverage(50, 50, 50, 80)).isCloseTo(74.0, within(0.05));
        }

        @Test
        @DisplayName("groups minutes by the hour of the site's timezone")
        void groupsByLocalHour() {
            List<ReadingPoint> series = new ArrayList<>();
            OffsetDateTime eightInLima = OffsetDateTime.of(2026, 10, 12, 13, 0, 0, 0, ZoneOffset.UTC);
            for (int minute = 0; minute < 60; minute++) {
                series.add(new ReadingPoint(eightInLima.plusMinutes(minute), 50f, null, 22f, 6f, 40f));
            }
            for (int minute = 0; minute < 10; minute++) {
                series.add(new ReadingPoint(eightInLima.plusHours(2).plusMinutes(minute), 50f, null, 22f, 6f, 40f));
            }

            HourlyProfile profile = service.hourlyProfile(UUID.randomUUID(), DAY, series);

            assertThat(profile.hours()).hasSize(24);
            HourSlot eight = profile.hours().get(8);
            assertThat(eight.verdict()).isEqualTo(Verdict.CONCLUSIVE);
            assertThat(eight.sampleSize()).isEqualTo(60);
            assertThat(eight.avgLaeq()).isEqualTo(50.0);
            assertThat(eight.avgOccupiedPct()).isEqualTo(40.0);
            assertThat(profile.hours().get(10).verdict()).isEqualTo(Verdict.NOT_ENOUGH_EVIDENCE);
            assertThat(profile.hours().get(10).avgLaeq()).isNull();
            assertThat(profile.hours().get(13).verdict()).isEqualTo(Verdict.NO_DATA);
        }
    }

    @Nested
    @DisplayName("site ranking")
    class Ranking {

        @Test
        @DisplayName("puts the most time above threshold first and rooms without data last")
        void worstFirst() {
            RoomRanking quiet = ranking("quiet", new NoiseDistribution(45, 50, 52, 60, 55f, 3, 5.0));
            RoomRanking noisy = ranking("noisy", new NoiseDistribution(55, 62, 65, 60, 55f, 18, 30.0));
            RoomRanking unclassified = ranking("unclassified", new NoiseDistribution(50, 70, 72, 60, null, null, null));
            RoomRanking silent = ranking("silent", null);

            List<RoomRanking> ranked = service.rank(List.of(silent, quiet, unclassified, noisy));

            assertThat(ranked).extracting(r -> r.room().code())
                    .containsExactly("noisy", "quiet", "unclassified", "silent");
        }

        private RoomRanking ranking(String code, NoiseDistribution distribution) {
            return new RoomRanking(new AnalyzedRoom(UUID.randomUUID(), code, null, null, LIMA),
                    DataQuality.of(1440, distribution == null ? 0 : 60), distribution);
        }
    }

    private static List<ReadingPoint> minutes(int fromMinute, int count, float baseDba, float occupiedPct) {
        List<ReadingPoint> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float jitter = ((i % 3) - 1) * 0.1f;
            points.add(point(fromMinute + i, baseDba + jitter, occupiedPct));
        }
        return points;
    }

    private static ReadingPoint point(int minute, float laeq, float occupiedPct) {
        return new ReadingPoint(START.plusMinutes(minute), laeq, null, 22.0f, 6.0f, occupiedPct);
    }
}
