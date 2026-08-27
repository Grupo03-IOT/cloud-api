package com.pe.cloudapi.monitoring.domain.model.commands;

import java.time.OffsetDateTime;

/**
 * Registra que un dispositivo acaba de enviar un lote.
 *
 * <p>El número de secuencia sirve para detectar huecos: si llega el 45 después
 * del 43, se contabiliza un lote perdido.
 */
public record RecordDeviceBatchCommand(
        String deviceCode,
        long seq,
        OffsetDateTime seenAt,
        String fwVersion
) {}
