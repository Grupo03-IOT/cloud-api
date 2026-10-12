package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.RecordHeartbeatCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.GatewayStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gateway aggregate")
class GatewayTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 10, 12, 12, 0, 0, 0, ZoneOffset.UTC);

    private final UUID siteId = UUID.randomUUID();

    @Test
    @DisplayName("is online right after a heartbeat, stale after a missed one, offline after three")
    void statusIsDerivedFromSilence() {
        Gateway gateway = new Gateway(heartbeat(NOW, 0));

        assertThat(gateway.statusAt(NOW.plusSeconds(60))).isEqualTo(GatewayStatus.ONLINE);
        assertThat(gateway.statusAt(NOW.plusSeconds(150))).isEqualTo(GatewayStatus.STALE);
        assertThat(gateway.statusAt(NOW.plusMinutes(4))).isEqualTo(GatewayStatus.OFFLINE);
    }

    @Test
    @DisplayName("keeps the newest heartbeat when an older one arrives late")
    void ignoresOlderHeartbeat() {
        Gateway gateway = new Gateway(heartbeat(NOW, 0));

        gateway.handle(heartbeat(NOW.minusMinutes(2), 40));

        assertThat(gateway.getLastSeen()).isEqualTo(NOW);
        assertThat(gateway.getPendingUpload()).isZero();
    }

    @Test
    @DisplayName("reflects the queue the Edge reports")
    void reflectsPendingUpload() {
        Gateway gateway = new Gateway(heartbeat(NOW, 0));

        gateway.handle(heartbeat(NOW.plusMinutes(1), 12));

        assertThat(gateway.getPendingUpload()).isEqualTo(12);
        assertThat(gateway.getLastSeen()).isEqualTo(NOW.plusMinutes(1));
    }

    private RecordHeartbeatCommand heartbeat(OffsetDateTime at, int pendingUpload) {
        return new RecordHeartbeatCommand("edge-lima-centro", siteId, "0.1.0", pendingUpload, 3, at);
    }
}
