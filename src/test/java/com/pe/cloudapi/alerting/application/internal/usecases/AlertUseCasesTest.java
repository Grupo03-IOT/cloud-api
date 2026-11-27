package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.queries.GetAlertQuery;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertClosure;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;
import com.pe.cloudapi.alerting.domain.ports.out.UserDirectory;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Alert use cases (in-memory repository)")
class AlertUseCasesTest {

    private static final OffsetDateTime OPENED = OffsetDateTime.parse("2026-11-08T14:10:00Z");

    private final UUID roomId = UUID.randomUUID();
    private final InMemoryAlerts alerts = new InMemoryAlerts();
    private final RoomDirectory rooms = new RoomDirectory() {
        @Override
        public UUID roomIdOf(String siteCode, String roomCode) {
            if (!roomCode.equals("sala-03")) throw MonitoringError.ROOM_NOT_FOUND.with(roomCode);
            return roomId;
        }

        @Override
        public Map<UUID, String> roomCodes() {
            return Map.of(roomId, "sala-03");
        }
    };

    private final Map<UUID, String> accounts = new HashMap<>();
    private final List<Collection<UUID>> nameLookups = new ArrayList<>();
    private final UserDirectory users = ids -> {
        nameLookups.add(ids);
        Map<UUID, String> found = new HashMap<>(accounts);
        found.keySet().retainAll(ids);
        return found;
    };
    private final RoomAlerts roomAlerts = new RoomAlerts(rooms, users);

    private final RecordAlertEventsUseCaseImpl record = new RecordAlertEventsUseCaseImpl(alerts, rooms);
    private final AcknowledgeAlertUseCaseImpl acknowledge = new AcknowledgeAlertUseCaseImpl(alerts, roomAlerts);
    private final ListAlertsUseCaseImpl list = new ListAlertsUseCaseImpl(alerts, roomAlerts);
    private final GetAlertUseCaseImpl get = new GetAlertUseCaseImpl(alerts, roomAlerts);

    @Test
    @DisplayName("the same source_key twice stores a single alert")
    void idempotentBySourceKey() {
        assertThat(record.execute(List.of(event("edge:1", "sala-03", null)))).isEqualTo(1);
        assertThat(record.execute(List.of(event("edge:1", "sala-03", null)))).isEqualTo(1);

        assertThat(alerts.stored).hasSize(1);
        assertThat(alerts.stored.getFirst().getStatus()).isEqualTo(AlertStatus.OPEN);
    }

    @Test
    @DisplayName("the closing event closes the stored alert automatically")
    void closingEventCloses() {
        record.execute(List.of(event("edge:1", "sala-03", null)));

        record.execute(List.of(event("edge:1", "sala-03", OPENED.plusMinutes(4))));

        Alert stored = alerts.stored.getFirst();
        assertThat(stored.getStatus()).isEqualTo(AlertStatus.CLOSED);
        assertThat(stored.getClosedBy()).isEqualTo(AlertClosure.AUTO);
    }

    @Test
    @DisplayName("a batch with an unknown room is rejected entirely")
    void unknownRoomRejectsBatch() {
        assertThatThrownBy(() -> record.execute(List.of(
                event("edge:1", "sala-03", null), event("edge:2", "sala-99", null))))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(MonitoringError.ROOM_NOT_FOUND);

        assertThat(alerts.stored).isEmpty();
    }

    @Test
    @DisplayName("acknowledging returns the alert with its room code; a second time is 409")
    void acknowledgeOnce() {
        record.execute(List.of(event("edge:1", "sala-03", null)));
        UUID id = alerts.stored.getFirst().getId();
        UUID admin = UUID.randomUUID();

        var result = acknowledge.execute(new AcknowledgeAlertCommand(id, admin, "Asked for silence"));

        assertThat(result.roomCode()).isEqualTo("sala-03");
        assertThat(result.alert().getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThatThrownBy(() -> acknowledge.execute(new AcknowledgeAlertCommand(id, admin, "Again")))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_NOT_OPEN);
    }

    @Test
    @DisplayName("acknowledging an unknown alert is 404")
    void acknowledgeUnknown() {
        assertThatThrownBy(() -> acknowledge.execute(
                new AcknowledgeAlertCommand(UUID.randomUUID(), UUID.randomUUID(), "Asked for silence")))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_NOT_FOUND);
    }

    @Test
    @DisplayName("lists newest first, filtering by status")
    void listsByStatus() {
        record.execute(List.of(
                event("edge:1", "sala-03", null),
                eventAt("edge:2", OPENED.plusHours(1), null),
                eventAt("edge:3", OPENED.plusHours(2), OPENED.plusHours(3))));

        var open = list.execute(new ListAlertsQuery(AlertStatus.OPEN, null, null, null, null));

        assertThat(open).extracting(r -> r.alert().getSourceKey()).containsExactly("edge:2", "edge:1");
        assertThat(open).allMatch(r -> "sala-03".equals(r.roomCode()));
    }

    @Test
    @DisplayName("an acknowledged alert carries the full name of who acknowledged it")
    void namesTheAcknowledger() {
        record.execute(List.of(event("edge:1", "sala-03", null)));
        UUID id = alerts.stored.getFirst().getId();
        UUID admin = UUID.randomUUID();
        accounts.put(admin, "Ana Torres");

        var result = acknowledge.execute(new AcknowledgeAlertCommand(id, admin, "Asked for silence"));

        assertThat(result.acknowledgedByName()).isEqualTo("Ana Torres");
        assertThat(get.execute(new GetAlertQuery(id)).acknowledgedByName()).isEqualTo("Ana Torres");
    }

    @Test
    @DisplayName("no name when nobody acknowledged it or the account no longer exists")
    void noNameWithoutAcknowledger() {
        record.execute(List.of(
                event("edge:1", "sala-03", null), eventAt("edge:2", OPENED.plusHours(1), null)));
        UUID id = alerts.stored.stream().filter(a -> a.getSourceKey().equals("edge:1"))
                .findFirst().orElseThrow().getId();

        assertThat(get.execute(new GetAlertQuery(id)).acknowledgedByName()).isNull();
        assertThat(nameLookups).isEmpty();

        var gone = acknowledge.execute(new AcknowledgeAlertCommand(id, UUID.randomUUID(), "Asked for silence"));

        assertThat(gone.acknowledgedByName()).isNull();
        assertThat(list.execute(new ListAlertsQuery(null, null, null, null, null)))
                .extracting(RoomAlert::acknowledgedByName).containsOnlyNulls();
    }

    @Test
    @DisplayName("a list asks for all the names in a single lookup")
    void oneNameLookupPerList() {
        record.execute(List.of(
                event("edge:1", "sala-03", null),
                eventAt("edge:2", OPENED.plusHours(1), null),
                eventAt("edge:3", OPENED.plusHours(2), null),
                eventAt("edge:4", OPENED.plusHours(3), null)));
        UUID ana = UUID.randomUUID();
        UUID luis = UUID.randomUUID();
        accounts.put(ana, "Ana Torres");
        accounts.put(luis, "Luis Rojas");
        acknowledgeBySourceKey("edge:1", ana);
        acknowledgeBySourceKey("edge:2", luis);
        acknowledgeBySourceKey("edge:3", ana);
        nameLookups.clear();

        var found = list.execute(new ListAlertsQuery(null, null, null, null, null));

        assertThat(found).extracting(RoomAlert::acknowledgedByName)
                .containsExactly(null, "Ana Torres", "Luis Rojas", "Ana Torres");
        assertThat(nameLookups).singleElement()
                .satisfies(ids -> assertThat(ids).containsExactlyInAnyOrder(ana, luis));
    }

    private void acknowledgeBySourceKey(String sourceKey, UUID userId) {
        UUID id = alerts.findBySourceKey(sourceKey).orElseThrow().getId();
        acknowledge.execute(new AcknowledgeAlertCommand(id, userId, "Asked for silence"));
    }

    @Test
    @DisplayName("an inverted range is rejected")
    void invertedRange() {
        assertThatThrownBy(() -> new ListAlertsQuery(null, null, OPENED, OPENED.minusDays(1), null))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.RANGE_INVERTED);
    }

    private static RecordAlertEventCommand event(String key, String room, OffsetDateTime closedAt) {
        return new RecordAlertEventCommand(key, null, room, "sustained_noise", AlertSeverity.WARNING,
                "Noise above 60 dBA for 2 min", 66.4, OPENED, closedAt);
    }

    private static RecordAlertEventCommand eventAt(String key, OffsetDateTime opened, OffsetDateTime closed) {
        return new RecordAlertEventCommand(key, null, "sala-03", "sustained_noise", AlertSeverity.WARNING,
                "Noise above 60 dBA for 2 min", 66.4, opened, closed);
    }

    /** Fake repository: keeps aggregates in a list and assigns ids on first save. */
    private static final class InMemoryAlerts implements AlertRepository {

        final List<Alert> stored = new ArrayList<>();

        @Override
        public Alert save(Alert alert) {
            Alert withId = alert.getId() == null ? alert.toBuilder().id(UUID.randomUUID()).build() : alert;
            stored.removeIf(existing -> existing.getId().equals(withId.getId()));
            stored.add(withId);
            return withId;
        }

        @Override
        public Optional<Alert> findById(UUID id) {
            return stored.stream().filter(a -> a.getId().equals(id)).findFirst()
                    .map(a -> a.toBuilder().build());
        }

        @Override
        public Optional<Alert> findBySourceKey(String sourceKey) {
            return stored.stream().filter(a -> a.getSourceKey().equals(sourceKey)).findFirst()
                    .map(a -> a.toBuilder().build());
        }

        @Override
        public List<Alert> search(ListAlertsQuery query) {
            return stored.stream()
                    .filter(a -> query.status() == null || a.getStatus() == query.status())
                    .sorted(Comparator.comparing(Alert::getOpenedAt).reversed())
                    .toList();
        }
    }
}
