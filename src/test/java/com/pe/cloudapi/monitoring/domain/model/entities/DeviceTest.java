package com.pe.cloudapi.monitoring.domain.model.entities;

import com.pe.cloudapi.monitoring.domain.model.commands.RegisterDeviceCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.SyncDeviceStateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Device entity")
class DeviceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 8, 26, 12, 0, 0, 0, ZoneOffset.UTC);

    private final UUID roomId = UUID.randomUUID();

    @Test
    @DisplayName("is registered linked to a room and has never reported")
    void registeredDeviceHasNeverReported() {
        Device device = new Device(new RegisterDeviceCommand("esp32-sala-01", roomId));

        assertThat(device.getCode()).isEqualTo("esp32-sala-01");
        assertThat(device.getRoomId()).isEqualTo(roomId);
        assertThat(device.getLostBatches()).isZero();
        assertThat(device.hasEverReported()).isFalse();
    }

    @Test
    @DisplayName("ignores a report with an older sequence so its state never goes back")
    void ignoresOutOfOrderReport() {
        Device device = new Device(new RegisterDeviceCommand("esp32-sala-01", roomId));
        device.handle(new SyncDeviceStateCommand("esp32-sala-01", roomId, "1.2.0", NOW, 42L, 3L));

        device.handle(new SyncDeviceStateCommand(
                "esp32-sala-01", roomId, "1.1.0", NOW.minusMinutes(5), 10L, 0L));

        assertThat(device.hasEverReported()).isTrue();
        assertThat(device.getLastSeq()).isEqualTo(42L);
        assertThat(device.getFwVersion()).isEqualTo("1.2.0");
        assertThat(device.getLastSeen()).isEqualTo(NOW);
    }
}
