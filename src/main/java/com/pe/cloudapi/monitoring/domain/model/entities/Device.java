package com.pe.cloudapi.monitoring.domain.model.entities;

import com.pe.cloudapi.monitoring.domain.model.commands.RecordDeviceBatchCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.RegisterDeviceCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un módulo ESP32 que reporta por una sala.
 *
 * <p>Se mantiene separado de la sala a propósito: cambiar un módulo quemado no
 * puede costarle a la sala su historia.
 *
 * <p>Se lleva cuenta del número de secuencia de los lotes para detectar
 * huecos, que es como se hace visible la pérdida de paquetes entre el
 * dispositivo y el Edge.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Device {

    /** Valor de {@code lastSeq} cuando el dispositivo aún no ha enviado nada. */
    private static final long NEVER_REPORTED = -1L;

    private final UUID id;

    /**
     * Identificador configurado en el firmware, del estilo {@code esp32-sala-01}.
     * Es la clave por la que llega en cada lote; el UUID no lo conoce.
     */
    private final String code;

    @Setter private UUID roomId;

    @Setter private String fwVersion;

    /** Instante del último lote recibido. Nulo si nunca ha reportado. */
    @Setter private OffsetDateTime lastSeen;

    /**
     * Número de secuencia del último lote recibido, o {@link #NEVER_REPORTED}
     * si todavía no ha llegado ninguno.
     */
    @Setter private long lastSeq;

    /**
     * Lotes que nunca llegaron, deducidos de los saltos en la secuencia.
     * Es acumulativo durante toda la vida del dispositivo.
     */
    @Setter private long lostBatches;

    @Setter private DeviceStatus status;

    /**
     * Da de alta un dispositivo que todavía no ha reportado nada.
     *
     * @param command alta con el código del firmware y la sala asociada
     */
    public Device(RegisterDeviceCommand command) {
        this.id = null;
        this.code = command.code();
        this.roomId = command.roomId();
        this.lastSeq = NEVER_REPORTED;
        this.lostBatches = 0L;
        this.status = DeviceStatus.UNKNOWN;
    }

    /**
     * Registra la llegada de un lote y contabiliza los que se perdieron.
     *
     * <p>El dispositivo numera sus lotes de forma correlativa, así que un salto
     * en la secuencia significa que algo no llegó: si el último recibido fue el
     * 43 y entra el 46, se perdieron dos. Esa cuenta es la única forma de saber
     * que hubo pérdida, porque un lote que no llega no deja ningún otro rastro.
     *
     * <p>El primer lote tras el alta no cuenta como pérdida, aunque su
     * secuencia sea alta: el dispositivo pudo llevar tiempo funcionando antes
     * de que el cloud supiera de él.
     *
     * @param command lote recibido, con su secuencia, instante y versión de
     *                firmware
     */
    public void handle(RecordDeviceBatchCommand command) {
        if (lastSeq > NEVER_REPORTED && command.seq() > lastSeq + 1) {
            lostBatches += command.seq() - lastSeq - 1;
        }
        this.lastSeq = command.seq();
        this.lastSeen = command.seenAt();
        this.fwVersion = command.fwVersion();
        this.status = DeviceStatus.ONLINE;
    }

    /**
     * Indica si el dispositivo ha enviado algún lote alguna vez.
     *
     * @return {@code false} mientras siga recién dado de alta
     */
    public boolean hasEverReported() {
        return lastSeq > NEVER_REPORTED;
    }
}
