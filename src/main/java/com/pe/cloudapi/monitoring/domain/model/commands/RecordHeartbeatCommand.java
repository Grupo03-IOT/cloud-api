package com.pe.cloudapi.monitoring.domain.model.commands;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un latido del Edge.
 *
 * @param code          identificador estable del Edge, del estilo {@code edge-lima-centro}
 * @param siteId        local en el que está instalado
 * @param version       versión del software del Edge
 * @param pendingUpload minutos en su cola sin subir: si crece con latidos llegando,
 *                      el cloud le está rechazando los lotes
 * @param roomsReported salas que el Edge atiende
 * @param receivedAt    cuándo lo recibió el cloud
 */
public record RecordHeartbeatCommand(
        String code,
        UUID siteId,
        String version,
        int pendingUpload,
        int roomsReported,
        OffsetDateTime receivedAt
) {}
