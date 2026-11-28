package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Discrepancy between a discomfort report and the reading")
class ComfortThresholdsTest {

    private static final ComfortThresholds LIMITS = new ComfortThresholds(55f, 26f, 20f);

    @ParameterizedTest(name = "{0} with laeq={1} temp={2} -> {3}")
    @CsvSource(nullValues = "none", value = {
            "NOISE, 48.0, 30.0, NOISE_WITHIN_RANGE",
            "NOISE, 55.0, 30.0, NOISE_WITHIN_RANGE",
            "NOISE, 55.1, 22.0, none",
            "WARM,  80.0, 24.9, TEMPERATURE_WITHIN_RANGE",
            "WARM,  80.0, 26.0, TEMPERATURE_WITHIN_RANGE",
            "WARM,  40.0, 26.1, none",
            "COLD,  80.0, 23.0, TEMPERATURE_WITHIN_RANGE",
            "COLD,  80.0, 20.0, TEMPERATURE_WITHIN_RANGE",
            "COLD,  40.0, 19.9, none",
            "NOISE, none, 22.0, none",
            "WARM,  48.0, none, none",
            "COLD,  48.0, none, none"
    })
    void comparesTheReportedMetric(DiscomfortCategory category, Float laeq, Float tempC,
                                   DiscrepancyReason expected) {
        var reading = new ReadingAtReport(OffsetDateTime.parse("2026-11-08T15:00:00Z"), laeq, tempC, null, null);

        assertThat(LIMITS.discrepancyWith(category, reading).orElse(null)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(DiscomfortCategory.class)
    @DisplayName("no reading means no discrepancy")
    void noReading(DiscomfortCategory category) {
        assertThat(LIMITS.discrepancyWith(category, null)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(DiscomfortCategory.class)
    @DisplayName("no threshold means no discrepancy")
    void noThreshold(DiscomfortCategory category) {
        var reading = new ReadingAtReport(OffsetDateTime.parse("2026-11-08T15:00:00Z"), 30f, 22f, null, null);

        assertThat(ComfortThresholds.none().discrepancyWith(category, reading)).isEmpty();
    }
}
