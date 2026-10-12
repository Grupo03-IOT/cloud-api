package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.RecordHeartbeatCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.GatewayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un Edge: el equipo del local que recibe a los dispositivos y sube al cloud.
 *
 * <p>Manda un latido cada minuto aunque no tenga nada que subir, así que su
 * silencio significa algo. El estado no se guarda: se deriva al consultar con
 * {@link #statusAt(OffsetDateTime)}.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Gateway {

    /** Hasta aquí sin latido, sigue en línea: tolera uno perdido. */
    public static final Duration GRACE = Duration.ofSeconds(90);

    /** Más allá de esto sin latido, se da por caído. */
    public static final Duration TOLERANCE = Duration.ofMinutes(3);

    private final UUID id;
    private final String code;
    private UUID siteId;
    private String version;
    private OffsetDateTime lastSeen;
    private int pendingUpload;
    private int roomsReported;

    public Gateway(RecordHeartbeatCommand command) {
        this.id = null;
        this.code = command.code();
        handle(command);
    }

    /**
     * Refleja un latido. Uno más viejo que el último conocido se ignora: llega
     * así cuando el Edge reintenta tras un corte.
     */
    public void handle(RecordHeartbeatCommand command) {
        if (lastSeen != null && command.receivedAt().isBefore(lastSeen)) {
            return;
        }
        this.siteId = command.siteId();
        this.version = command.version();
        this.lastSeen = command.receivedAt();
        this.pendingUpload = command.pendingUpload();
        this.roomsReported = command.roomsReported();
    }

    /**
     * @param now el instante de la consulta; entra como parámetro para no depender del reloj
     */
    public GatewayStatus statusAt(OffsetDateTime now) {
        if (lastSeen == null) {
            return GatewayStatus.OFFLINE;
        }
        Duration silence = Duration.between(lastSeen, now);
        if (silence.compareTo(GRACE) <= 0) {
            return GatewayStatus.ONLINE;
        }
        if (silence.compareTo(TOLERANCE) <= 0) {
            return GatewayStatus.STALE;
        }
        return GatewayStatus.OFFLINE;
    }
}
