package com.pe.cloudapi.alerting.domain.model.aggregates;

import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertClosure;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Alert aggregate")
class AlertTest {

    private static final OffsetDateTime OPENED = OffsetDateTime.parse("2026-11-08T14:10:00Z");
    private static final OffsetDateTime CLOSED = OPENED.plusMinutes(6);

    private final UUID roomId = UUID.randomUUID();
    private final UUID admin = UUID.randomUUID();

    @Test
    @DisplayName("opens as OPEN with the data the Edge sent")
    void opens() {
        Alert alert = new Alert(event(null), roomId);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getRoomId()).isEqualTo(roomId);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(alert.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("is born CLOSED when the first event already carries the closing")
    void bornClosed() {
        Alert alert = new Alert(event(CLOSED), roomId);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.CLOSED);
        assertThat(alert.getClosedBy()).isEqualTo(AlertClosure.AUTO);
    }

    @Test
    @DisplayName("a repeated opening event changes nothing")
    void repeatedOpeningIsIgnored() {
        Alert alert = new Alert(event(null), roomId);

        alert.handle(event(null));

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getClosedAt()).isNull();
    }

    @Test
    @DisplayName("acknowledges an open alert, recording who and the corrective action")
    void acknowledges() {
        Alert alert = new Alert(event(null), roomId);

        alert.acknowledge(new AcknowledgeAlertCommand(null, admin, "  Asked for silence  "), CLOSED);

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedBy()).isEqualTo(admin);
        assertThat(alert.getAcknowledgedAt()).isEqualTo(CLOSED);
        assertThat(alert.getCorrectiveAction()).isEqualTo("Asked for silence");
    }

    @Test
    @DisplayName("cannot be acknowledged twice")
    void acknowledgeOnlyFromOpen() {
        Alert alert = new Alert(event(null), roomId);
        alert.acknowledge(new AcknowledgeAlertCommand(null, admin, "Asked for silence"), CLOSED);

        assertThatThrownBy(() -> alert.acknowledge(
                new AcknowledgeAlertCommand(null, admin, "Again"), CLOSED))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_NOT_OPEN);
    }

    @Test
    @DisplayName("cannot be acknowledged once closed")
    void closedCannotBeAcknowledged() {
        Alert alert = new Alert(event(CLOSED), roomId);

        assertThatThrownBy(() -> alert.acknowledge(
                new AcknowledgeAlertCommand(null, admin, "Too late"), CLOSED))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_NOT_OPEN);
    }

    @Test
    @DisplayName("requires a corrective action of 3 to 500 characters")
    void actionIsRequired() {
        Alert alert = new Alert(event(null), roomId);

        for (String action : new String[]{null, "", "   ", "ok", "x".repeat(501)}) {
            assertThatThrownBy(() -> alert.acknowledge(
                    new AcknowledgeAlertCommand(null, admin, action), CLOSED))
                    .extracting(ex -> ((DomainException) ex).getError())
                    .isEqualTo(AlertingError.CORRECTIVE_ACTION_REQUIRED);
        }
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
    }

    @Test
    @DisplayName("closes automatically from ACKNOWLEDGED, keeping the corrective action")
    void closesAfterAcknowledge() {
        Alert alert = new Alert(event(null), roomId);
        alert.acknowledge(new AcknowledgeAlertCommand(null, admin, "Asked for silence"), OPENED.plusMinutes(1));

        alert.handle(event(CLOSED));

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.CLOSED);
        assertThat(alert.getClosedBy()).isEqualTo(AlertClosure.AUTO);
        assertThat(alert.getCorrectiveAction()).isEqualTo("Asked for silence");
    }

    @Test
    @DisplayName("closing is idempotent: the first closing time is kept")
    void closingIsIdempotent() {
        Alert alert = new Alert(event(null), roomId);
        alert.closeAutomatically(CLOSED);

        alert.closeAutomatically(CLOSED.plusMinutes(5));

        assertThat(alert.getStatus()).isEqualTo(AlertStatus.CLOSED);
        assertThat(alert.getClosedAt()).isEqualTo(CLOSED);
    }

    @Test
    @DisplayName("rejects a closing before the opening")
    void closingBeforeOpening() {
        Alert alert = new Alert(event(null), roomId);

        assertThatThrownBy(() -> alert.closeAutomatically(OPENED.minusMinutes(1)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_CLOSED_BEFORE_OPENED);
        assertThatThrownBy(() -> event(OPENED.minusMinutes(1)))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(AlertingError.ALERT_CLOSED_BEFORE_OPENED);
    }

    private static RecordAlertEventCommand event(OffsetDateTime closedAt) {
        return new RecordAlertEventCommand("edge-lima-centro:42", null, "sala-03", "sustained_noise",
                AlertSeverity.WARNING, "Noise above 60 dBA for 2 min", 66.4, OPENED, closedAt);
    }
}
