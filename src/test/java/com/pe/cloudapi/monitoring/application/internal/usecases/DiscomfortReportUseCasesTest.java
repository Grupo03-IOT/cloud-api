package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ComfortThresholds;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscrepancyReason;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.monitoring.domain.ports.out.ComfortThresholdProvider;
import com.pe.cloudapi.monitoring.domain.ports.out.DiscomfortReportRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Discomfort report use cases")
class DiscomfortReportUseCasesTest {

    private final RoomRepository rooms = mock(RoomRepository.class);
    private final RoomReadingRepository readings = mock(RoomReadingRepository.class);
    private final DiscomfortReportRepository reports = mock(DiscomfortReportRepository.class);
    private final ComfortThresholdProvider thresholds = mock(ComfortThresholdProvider.class);
    private final RoomDiscomfortReports roomReports = new RoomDiscomfortReports(rooms, thresholds);
    private final FileDiscomfortReportUseCaseImpl file =
            new FileDiscomfortReportUseCaseImpl(rooms, readings, reports, roomReports);
    private final ListDiscomfortReportsUseCaseImpl list = new ListDiscomfortReportsUseCaseImpl(reports, roomReports);

    private final UUID roomA = UUID.randomUUID();
    private final UUID roomB = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("an unknown room is 404 and nothing is stored")
    void unknownRoom() {
        when(rooms.findById(roomA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> file.execute(command(DiscomfortCategory.WARM)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.ROOM_NOT_FOUND);
        verify(reports, never()).save(any());
    }

    @Test
    @DisplayName("a room without readings is reported with reading null")
    void noReading() {
        when(rooms.findById(roomA)).thenReturn(Optional.of(Room.builder().id(roomA).code("sala-03").build()));
        when(readings.findLatest(roomA)).thenReturn(Optional.empty());
        when(reports.findLatest(userId, roomA, DiscomfortCategory.WARM)).thenReturn(Optional.empty());
        when(reports.save(any())).thenAnswer(call -> ((DiscomfortReport) call.getArgument(0))
                .toBuilder().id(UUID.randomUUID()).build());

        var result = file.execute(command(DiscomfortCategory.WARM));

        assertThat(result.roomCode()).isEqualTo("sala-03");
        assertThat(result.report().getReading()).isNull();
        assertThat(result.report().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("a second report within 10 minutes is 409")
    void tooFrequent() {
        when(rooms.findById(roomA)).thenReturn(Optional.of(Room.builder().id(roomA).code("sala-03").build()));
        when(reports.findLatest(userId, roomA, DiscomfortCategory.WARM)).thenReturn(Optional.of(
                report(roomA, DiscomfortCategory.WARM, OffsetDateTime.now().minusMinutes(2))));

        assertThatThrownBy(() -> file.execute(command(DiscomfortCategory.WARM)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.DISCOMFORT_TOO_FREQUENT);
        verify(reports, never()).save(any());
    }

    @Test
    @DisplayName("the summary counts per room and category, most reported room first")
    void summary() {
        OffsetDateTime at = OffsetDateTime.parse("2026-11-08T15:00:00Z");
        when(reports.search(any(ListDiscomfortReportsQuery.class))).thenReturn(List.of(
                report(roomA, DiscomfortCategory.WARM, at),
                report(roomB, DiscomfortCategory.NOISE, at),
                report(roomB, DiscomfortCategory.NOISE, at),
                report(roomB, DiscomfortCategory.COLD, at)));
        when(rooms.findById(roomA)).thenReturn(Optional.of(Room.builder().id(roomA).code("sala-03").build()));
        when(rooms.findById(roomB)).thenReturn(Optional.of(Room.builder().id(roomB).code("sala-01").build()));

        var summary = list.summarize(null, null);

        assertThat(summary).hasSize(2);
        assertThat(summary.getFirst().roomCode()).isEqualTo("sala-01");
        assertThat(summary.getFirst().noise()).isEqualTo(2);
        assertThat(summary.getFirst().cold()).isEqualTo(1);
        assertThat(summary.getFirst().total()).isEqualTo(3);
        assertThat(summary.get(1).warm()).isEqualTo(1);
        assertThat(summary.get(1).total()).isEqualTo(1);
        assertThat(summary).extracting(DiscomfortSummary::discrepancies).containsOnly(0L);
    }

    @Test
    @DisplayName("a new report already says whether the reading backs it")
    void filedWithDiscrepancy() {
        UUID type = classifiedRoom(roomA, "sala-03");
        when(readings.findLatest(roomA)).thenReturn(Optional.of(RoomReading.builder().roomId(roomA)
                .ts(OffsetDateTime.now().minusMinutes(1)).climate(new Climate(24.9f, 58f)).build()));
        when(reports.findLatest(userId, roomA, DiscomfortCategory.WARM)).thenReturn(Optional.empty());
        when(reports.save(any())).thenAnswer(call -> ((DiscomfortReport) call.getArgument(0))
                .toBuilder().id(UUID.randomUUID()).build());
        when(thresholds.comfortThresholdsOf(type)).thenReturn(new ComfortThresholds(55f, 26f, 20f));

        var result = file.execute(command(DiscomfortCategory.WARM));

        assertThat(result.hasDiscrepancy()).isTrue();
        assertThat(result.discrepancyReason()).isEqualTo(DiscrepancyReason.TEMPERATURE_WITHIN_RANGE);
    }

    @Test
    @DisplayName("the discrepancy filter keeps only the matching reports; no filter keeps all")
    void filtersByDiscrepancy() {
        UUID type = classifiedRoom(roomA, "sala-03");
        when(thresholds.comfortThresholdsOf(type)).thenReturn(new ComfortThresholds(55f, 26f, 20f));
        DiscomfortReport quiet = reportWith(roomA, DiscomfortCategory.NOISE, 48f, 22f);
        DiscomfortReport loud = reportWith(roomA, DiscomfortCategory.NOISE, 70f, 22f);
        DiscomfortReport blind = report(roomA, DiscomfortCategory.WARM, OffsetDateTime.now());
        when(reports.search(any(ListDiscomfortReportsQuery.class))).thenReturn(List.of(quiet, loud, blind));

        assertThat(list.execute(query(true))).extracting(RoomDiscomfortReport::report).containsExactly(quiet);
        assertThat(list.execute(query(false))).extracting(RoomDiscomfortReport::report)
                .containsExactly(loud, blind);
        assertThat(list.execute(query(null))).hasSize(3);
    }

    @Test
    @DisplayName("a room without a type has no discrepancy and no threshold is looked up")
    void unclassifiedRoom() {
        when(rooms.findById(roomA)).thenReturn(Optional.of(Room.builder().id(roomA).code("sala-03").build()));
        when(reports.findByUserId(userId))
                .thenReturn(List.of(reportWith(roomA, DiscomfortCategory.NOISE, 30f, 22f)));

        assertThat(list.ofUser(userId)).singleElement()
                .satisfies(r -> assertThat(r.hasDiscrepancy()).isFalse());
        verifyNoInteractions(thresholds);
    }

    @Test
    @DisplayName("the summary counts the discrepancies per room, looking each type up once")
    void summaryCountsDiscrepancies() {
        UUID type = classifiedRoom(roomA, "sala-03");
        when(rooms.findById(roomB)).thenReturn(Optional.of(
                Room.builder().id(roomB).code("sala-01").roomTypeId(type).build()));
        when(thresholds.comfortThresholdsOf(type)).thenReturn(new ComfortThresholds(55f, 26f, 20f));
        when(reports.search(any(ListDiscomfortReportsQuery.class))).thenReturn(List.of(
                reportWith(roomA, DiscomfortCategory.NOISE, 48f, 22f),
                reportWith(roomA, DiscomfortCategory.COLD, 48f, 22f),
                reportWith(roomA, DiscomfortCategory.WARM, 48f, 29f),
                reportWith(roomB, DiscomfortCategory.COLD, 48f, 18f)));

        var summary = list.summarize(null, null);

        assertThat(summary).extracting(DiscomfortSummary::roomCode, DiscomfortSummary::total,
                        DiscomfortSummary::discrepancies)
                .containsExactly(tuple("sala-03", 3L, 2L), tuple("sala-01", 1L, 0L));
        verify(thresholds, times(1)).comfortThresholdsOf(type);
        verify(rooms, times(1)).findById(roomA);
    }

    private UUID classifiedRoom(UUID id, String code) {
        UUID type = UUID.randomUUID();
        when(rooms.findById(id)).thenReturn(Optional.of(
                Room.builder().id(id).code(code).roomTypeId(type).build()));
        return type;
    }

    private DiscomfortReport reportWith(UUID room, DiscomfortCategory category, float laeq, float tempC) {
        OffsetDateTime at = OffsetDateTime.parse("2026-11-08T15:00:00Z");
        return report(room, category, at).toBuilder()
                .reading(new ReadingAtReport(at.minusMinutes(1), laeq, tempC, null, null)).build();
    }

    private static ListDiscomfortReportsQuery query(Boolean discrepancy) {
        return new ListDiscomfortReportsQuery(null, null, null, null, discrepancy);
    }

    private FileDiscomfortReportCommand command(DiscomfortCategory category) {
        return new FileDiscomfortReportCommand(roomA, userId, category, null);
    }

    private DiscomfortReport report(UUID room, DiscomfortCategory category, OffsetDateTime at) {
        return DiscomfortReport.builder().id(UUID.randomUUID()).roomId(room).userId(userId)
                .category(category).createdAt(at).build();
    }
}
