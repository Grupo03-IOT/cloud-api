package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un módulo ESP32 y cómo le va.
 */
@Schema(description = "An ESP32 module and how it is doing")
public record DeviceResource(
        UUID id,
        @Schema(example = "esp32-sala-01") String code,
        UUID roomId,
        @Schema(example = "0.1.0") String fwVersion,
        @Schema(description = "Último lote que le llegó al Edge; no viene si nunca reportó")
        OffsetDateTime lastSeen,
        Long lastSeq,
        @Schema(description = "Lotes perdidos acumulados, contados por el Edge") long lostBatches,
        @Schema(description = "Más de cinco minutos sin reportar, o nunca reportó") boolean silent
) {}
