package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.AcknowledgeAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.ListAlertsUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.RecordAlertEventsUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertClosure;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * US20-US22 against PostgreSQL: the Edge opens an alert, the administrator
 * acknowledges it and the Edge closes it.
 */
@SpringBootTest
@Transactional
@DisplayName("Alert lifecycle on PostgreSQL (US20-US22)")
class AlertLifecycleIntegrationTest {

    private static final OffsetDateTime OPENED = OffsetDateTime.parse("2026-11-08T14:10:00Z");

    @Autowired RecordAlertEventsUseCase recordEvents;
    @Autowired ListAlertsUseCase listAlerts;
    @Autowired AcknowledgeAlertUseCase acknowledge;
    @Autowired SiteRepository sites;
    @Autowired RoomRepository rooms;

    @Test
    @DisplayName("open, retry, acknowledge and close automatically")
    void fullLifecycle() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Site site = sites.save(new Site(new CreateSiteCommand(
                "alert-" + suffix, "Alert test", "Laboratorio", "America/Lima")));
        Room room = rooms.save(new Room(CreateRoomCommand.autoRegistered(site.getId(), "sala-03")));
        String key = "edge-" + suffix + ":42";

        recordEvents.execute(List.of(event(key, site.getCode(), "sala-03", null)));
        recordEvents.execute(List.of(event(key, site.getCode(), "sala-03", null)));

        List<RoomAlert> open = listAlerts.execute(
                new ListAlertsQuery(AlertStatus.OPEN, room.getId(), null, null, null));
        assertThat(open).hasSize(1);
        assertThat(open.getFirst().alert().getRoomId()).isEqualTo(room.getId());
        UUID alertId = open.getFirst().alert().getId();

        UUID admin = UUID.randomUUID();
        acknowledge.execute(new AcknowledgeAlertCommand(alertId, admin, "Asked for silence in room 3"));
        assertThat(listAlerts.execute(new ListAlertsQuery(null, room.getId(), null, null, true)))
                .singleElement()
                .satisfies(r -> assertThat(r.alert().getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED));

        recordEvents.execute(List.of(event(key, site.getCode(), "sala-03", OPENED.plusMinutes(6))));

        List<RoomAlert> history = listAlerts.execute(new ListAlertsQuery(
                AlertStatus.CLOSED, room.getId(), OPENED.minusHours(1), OPENED.plusHours(1), null));
        assertThat(history).singleElement().satisfies(r -> {
            assertThat(r.alert().getId()).isEqualTo(alertId);
            assertThat(r.alert().getClosedBy()).isEqualTo(AlertClosure.AUTO);
            assertThat(r.alert().getAcknowledgedBy()).isEqualTo(admin);
            assertThat(r.alert().getCorrectiveAction()).isEqualTo("Asked for silence in room 3");
        });
        assertThat(listAlerts.execute(new ListAlertsQuery(null, room.getId(), null, null, false))).isEmpty();
    }

    @Test
    @DisplayName("an unknown room is rejected with MONITORING_ROOM_NOT_FOUND")
    void unknownRoom() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Site site = sites.save(new Site(new CreateSiteCommand(
                "alert-" + suffix, "Alert test", "Laboratorio", "America/Lima")));

        assertThatThrownBy(() -> recordEvents.execute(List.of(
                event("edge-" + suffix + ":1", site.getCode(), "sala-404", null))))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.ROOM_NOT_FOUND);
    }

    private static RecordAlertEventCommand event(String key, String site, String room, OffsetDateTime closedAt) {
        return new RecordAlertEventCommand(key, site, room, "sustained_noise", AlertSeverity.WARNING,
                "Noise above 60 dBA for 2 min", 66.4, OPENED, closedAt);
    }
}
