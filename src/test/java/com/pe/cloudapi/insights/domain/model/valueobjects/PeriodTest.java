package com.pe.cloudapi.insights.domain.model.valueobjects;

import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Named period")
class PeriodTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Test
    @DisplayName("a month starts at midnight in the site's timezone, not in UTC")
    void monthInLima() {
        PeriodWindow window = Period.parse("2026-10").in(LIMA);

        assertThat(window.from().toInstant())
                .isEqualTo(OffsetDateTime.of(2026, 10, 1, 5, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(window.to().toInstant())
                .isEqualTo(OffsetDateTime.of(2026, 11, 1, 5, 0, 0, 0, ZoneOffset.UTC).toInstant());
        assertThat(window.minutes()).isEqualTo(31 * 1440);
    }

    @Test
    @DisplayName("an ISO week starts on its Monday")
    void isoWeek() {
        Period week = Period.parse("2026-W42");

        assertThat(week.start()).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(week.end()).isEqualTo(LocalDate.of(2026, 10, 19));
        assertThat(week.label()).isEqualTo("2026-W42");
    }

    @Test
    @DisplayName("week 53 exists only in the years that have it")
    void week53() {
        assertThat(Period.parse("2026-W53").start()).isEqualTo(LocalDate.of(2026, 12, 28));
        assertThatThrownBy(() -> Period.parse("2027-W53")).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("a day is one day")
    void day() {
        Period day = Period.parse("2026-10-12");

        assertThat(day.in(LIMA).minutes()).isEqualTo(1440);
        assertThat(day.label()).isEqualTo("2026-10-12");
    }

    @Test
    @DisplayName("the previous period crosses years with the same granularity")
    void previous() {
        assertThat(Period.parse("2026-01").previous().label()).isEqualTo("2025-12");
        assertThat(Period.parse("2026-W01").previous().label()).isEqualTo("2025-W52");
        assertThat(Period.parse("2026-03-01").previous().label()).isEqualTo("2026-02-28");
    }

    @Test
    @DisplayName("anything else is rejected as an invalid period")
    void invalid() {
        for (String text : new String[]{"2026-13", "2026-02-30", "2026-W00", "october", "2026/10", "", null}) {
            assertThatThrownBy(() -> Period.parse(text))
                    .as(String.valueOf(text))
                    .isInstanceOf(DomainException.class)
                    .extracting(ex -> ((DomainException) ex).getCode())
                    .isEqualTo("INSIGHTS_PERIOD_INVALID");
        }
    }
}
