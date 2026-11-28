package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.FileDiscomfortReportUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.IngestReadingsUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDiscomfortReportsUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.pe.cloudapi.alerting.application.internal.ports.in.ConfigureThresholdUseCase;
import com.pe.cloudapi.alerting.domain.model.commands.ConfigureThresholdCommand;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ClassifyRoomUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.commands.ClassifyRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomTypeCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscrepancyReason;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US27-US28 against PostgreSQL: a member reports, the reading of the moment
 * is kept even after the room changes, and the administrator sees it.
 */
@SpringBootTest
@Transactional
@DisplayName("Discomfort reports on PostgreSQL (US27-US28)")
class DiscomfortReportIntegrationTest {

    @Autowired FileDiscomfortReportUseCase file;
    @Autowired ListDiscomfortReportsUseCase list;
    @Autowired IngestReadingsUseCase ingest;
    @Autowired SiteRepository sites;
    @Autowired RoomRepository rooms;
    @Autowired RoomTypeRepository roomTypes;
    @Autowired ClassifyRoomUseCase classify;
    @Autowired ConfigureThresholdUseCase configure;

    @Test
    @DisplayName("stores the reading of the moment, limits frequency and summarises per room")
    void reportFlow() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Site site = sites.save(new Site(new CreateSiteCommand(
                "disc-" + suffix, "Discomfort test", "Laboratorio", "America/Lima")));
        OffsetDateTime minute = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES).minusMinutes(1);
        ingest.execute(reading(site.getCode(), minute, 24.9f));
        Room room = rooms.findBySiteIdAndCode(site.getId(), "sala-03").orElseThrow();
        UUID member = UUID.randomUUID();

        var created = file.execute(new FileDiscomfortReportCommand(
                room.getId(), member, DiscomfortCategory.WARM, "Hace calor cerca de la ventana"));
        assertThat(created.report().getId()).isNotNull();
        assertThat(created.roomCode()).isEqualTo("sala-03");
        assertThat(created.report().getReading().tempC()).isEqualTo(24.9f);

        assertThatThrownBy(() -> file.execute(new FileDiscomfortReportCommand(
                room.getId(), member, DiscomfortCategory.WARM, null)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.DISCOMFORT_TOO_FREQUENT);
        file.execute(new FileDiscomfortReportCommand(room.getId(), member, DiscomfortCategory.NOISE, null));

        ingest.execute(reading(site.getCode(), minute, 30f));

        var adminView = list.execute(new ListDiscomfortReportsQuery(
                room.getId(), DiscomfortCategory.WARM, null, null, null));
        assertThat(adminView).singleElement()
                .satisfies(r -> assertThat(r.report().getReading().tempC()).isEqualTo(24.9f));
        assertThat(list.ofUser(member)).hasSize(2);
        assertThat(list.summarize(null, null))
                .filteredOn(s -> s.roomId().equals(room.getId()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.warm()).isEqualTo(1);
                    assertThat(s.noise()).isEqualTo(1);
                    assertThat(s.total()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("marks the reports that the room type thresholds do not back")
    void discrepancyAgainstRoomTypeThresholds() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Site site = sites.save(new Site(new CreateSiteCommand(
                "disc-" + suffix, "Discrepancy test", "Laboratorio", "America/Lima")));
        OffsetDateTime minute = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES).minusMinutes(1);
        ingest.execute(reading(site.getCode(), minute, 24.9f));
        Room room = rooms.findBySiteIdAndCode(site.getId(), "sala-03").orElseThrow();
        UUID member = UUID.randomUUID();
        file.execute(new FileDiscomfortReportCommand(room.getId(), member, DiscomfortCategory.WARM, null));
        var inRoom = new ListDiscomfortReportsQuery(room.getId(), null, null, null, true);
        assertThat(list.execute(inRoom)).isEmpty();

        UUID type = roomTypes.save(new RoomType(new CreateRoomTypeCommand(
                site.getId(), "focus-" + suffix, "Focus", null))).getId();
        classify.execute(new ClassifyRoomCommand(room.getId(), type));
        configure.execute(new ConfigureThresholdCommand(type, ThresholdMetric.TEMP_C, 26f, 29f, 1, true));
        configure.execute(new ConfigureThresholdCommand(type, ThresholdMetric.LAEQ, 55f, null, 1, true));
        file.execute(new FileDiscomfortReportCommand(room.getId(), member, DiscomfortCategory.NOISE, null));

        assertThat(list.execute(inRoom)).singleElement().satisfies(r -> {
            assertThat(r.report().getCategory()).isEqualTo(DiscomfortCategory.WARM);
            assertThat(r.discrepancyReason()).isEqualTo(DiscrepancyReason.TEMPERATURE_WITHIN_RANGE);
        });
        assertThat(list.summarize(null, null))
                .filteredOn(s -> s.roomId().equals(room.getId()))
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.total()).isEqualTo(2);
                    assertThat(s.discrepancies()).isEqualTo(1);
                });
    }

    private static IngestReadingsCommand reading(String site, OffsetDateTime ts, float temperature) {
        return new IngestReadingsCommand(site, List.of(new IngestReadingsCommand.Reading("sala-03", ts, 60,
                null, null, new Climate(temperature, 58f), null, null, null, null, null)));
    }
}
