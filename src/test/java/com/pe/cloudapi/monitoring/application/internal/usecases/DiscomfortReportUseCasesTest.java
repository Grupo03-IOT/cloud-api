package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Discomfort report use cases")
class DiscomfortReportUseCasesTest {

    private final RoomRepository rooms = mock(RoomRepository.class);
    private final RoomReadingRepository readings = mock(RoomReadingRepository.class);
    private final DiscomfortReportRepository reports = mock(DiscomfortReportRepository.class);
    private final FileDiscomfortReportUseCaseImpl file = new FileDiscomfortReportUseCaseImpl(rooms, readings, reports);
    private final ListDiscomfortReportsUseCaseImpl list = new ListDiscomfortReportsUseCaseImpl(reports, rooms);

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
    }

    private FileDiscomfortReportCommand command(DiscomfortCategory category) {
        return new FileDiscomfortReportCommand(roomA, userId, category, null);
    }

    private DiscomfortReport report(UUID room, DiscomfortCategory category, OffsetDateTime at) {
        return DiscomfortReport.builder().id(UUID.randomUUID()).roomId(room).userId(userId)
                .category(category).createdAt(at).build();
    }
}
