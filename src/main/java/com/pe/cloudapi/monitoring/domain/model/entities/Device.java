package com.pe.cloudapi.monitoring.domain.model.entities;

import com.pe.cloudapi.monitoring.domain.model.commands.RegisterDeviceCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.SyncDeviceStateCommand;
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
 * <p>El cloud no ve los lotes uno a uno —solo el resumen por minuto que sube el
 * Edge—, así que la detección de huecos en la secuencia ocurre en el Edge y
 * aquí solo se refleja. Ver {@link SyncDeviceStateCommand}.
 *
 * <p>Esta entidad guarda <strong>observaciones</strong>, no interpretaciones:
 * cuándo se le oyó por última vez, con qué secuencia y cuántos lotes se
 * perdieron. Si un dispositivo está caído o no es una lectura de esos datos, y
 * se resolverá en la fase de alertas junto con el trabajo programado que avise
 * al administrador.
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

    /** Instante del último lote que le llegó al Edge. Nulo si nunca ha reportado. */
    @Setter private OffsetDateTime lastSeen;

    /**
     * Número de secuencia del último lote, o {@link #NEVER_REPORTED} si todavía
     * no ha llegado ninguno.
     */
    @Setter private long lastSeq;

    /** Lotes perdidos acumulados, contados por el Edge. */
    @Setter private long lostBatches;

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
    }

    /**
     * Refleja el estado que el Edge reporta del dispositivo.
     *
     * <p>Los contadores se copian, no se recalculan: el Edge es la única capa
     * que ve la secuencia completa de lotes.
     *
     * <p>Se ignoran los reportes que traigan una secuencia anterior a la ya
     * conocida. Ocurre cuando el Edge vacía una cola atrasada tras un corte de
     * red: llegan minutos viejos después de otros más nuevos, y sin esta guarda
     * el estado del dispositivo retrocedería.
     *
     * @param command estado reportado por el Edge
     */
    public void handle(SyncDeviceStateCommand command) {
        if (command.lastSeq() < this.lastSeq) {
            return;
        }
        this.roomId = command.roomId();
        this.fwVersion = command.fwVersion();
        this.lastSeen = command.lastSeen();
        this.lastSeq = command.lastSeq();
        this.lostBatches = command.lostBatches();
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
