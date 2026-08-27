package com.pe.cloudapi.monitoring.domain.model.commands;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Refleja en el cloud el estado que el Edge reporta de un dispositivo.
 *
 * <p>Los lotes perdidos llegan ya contados. El cloud <strong>no puede</strong>
 * deducirlos por su cuenta: solo ve un resumen por minuto, no la secuencia
 * completa de lotes. Quien sí la ve es el Edge, así que la cuenta se hace allí
 * y aquí se refleja tal cual.
 *
 * @param code        identificador del firmware, del estilo {@code esp32-sala-01}
 * @param roomId      sala por la que reporta
 * @param fwVersion   versión de firmware que declara
 * @param lastSeen    instante del último lote que le llegó al Edge
 * @param lastSeq     número de secuencia de ese lote; nulo si nunca reportó
 * @param lostBatches lotes perdidos acumulados, contados por el Edge
 */
public record SyncDeviceStateCommand(
        String code,
        UUID roomId,
        String fwVersion,
        OffsetDateTime lastSeen,
        Long lastSeq,
        long lostBatches
) {}
