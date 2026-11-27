package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DiscomfortReport aggregate")
class DiscomfortReportTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-08T15:00:00Z");

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("keeps the reading of the moment when it is recent")
    void keepsRecentReading() {
        ReadingAtReport reading = new ReadingAtReport(NOW.minusMinutes(1), 52.1f, 24.9f, 58f, 100f);

        DiscomfortReport report = new DiscomfortReport(command(" Hace calor "), Optional.empty(),
                Optional.of(reading), NOW);

        assertThat(report.getReading()).isEqualTo(reading);
        assertThat(report.getComment()).isEqualTo("Hace calor");
        assertThat(report.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("drops a reading older than 10 minutes")
    void dropsStaleReading() {
        ReadingAtReport stale = new ReadingAtReport(NOW.minusMinutes(11), 52.1f, 24.9f, 58f, 100f);

        DiscomfortReport report = new DiscomfortReport(command(null), Optional.empty(), Optional.of(stale), NOW);

        assertThat(report.getReading()).isNull();
    }

    @Test
    @DisplayName("rejects the same category in the same room within 10 minutes")
    void tooFrequent() {
        DiscomfortReport previous = new DiscomfortReport(command(null), Optional.empty(), Optional.empty(),
                NOW.minusMinutes(9));

        assertThatThrownBy(() -> new DiscomfortReport(command(null), Optional.of(previous),
                Optional.empty(), NOW))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.DISCOMFORT_TOO_FREQUENT);
    }

    @Test
    @DisplayName("accepts it again once 10 minutes have passed")
    void allowedAfterInterval() {
        DiscomfortReport previous = new DiscomfortReport(command(null), Optional.empty(), Optional.empty(),
                NOW.minusMinutes(10));

        DiscomfortReport report = new DiscomfortReport(command(null), Optional.of(previous),
                Optional.empty(), NOW);

        assertThat(report.getCategory()).isEqualTo(DiscomfortCategory.WARM);
    }

    @Test
    @DisplayName("a blank comment is stored as none; more than 500 characters is rejected")
    void commentRules() {
        assertThat(command("   ").comment()).isNull();
        assertThatThrownBy(() -> command("x".repeat(501)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.DISCOMFORT_COMMENT_TOO_LONG);
    }

    @Test
    @DisplayName("an unknown category is rejected")
    void unknownCategory() {
        assertThat(DiscomfortCategory.fromCode("cold")).isEqualTo(DiscomfortCategory.COLD);
        assertThatThrownBy(() -> DiscomfortCategory.fromCode("SMELL"))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.UNKNOWN_DISCOMFORT_CATEGORY);
    }

    private FileDiscomfortReportCommand command(String comment) {
        return new FileDiscomfortReportCommand(roomId, userId, DiscomfortCategory.WARM, comment);
    }
}
