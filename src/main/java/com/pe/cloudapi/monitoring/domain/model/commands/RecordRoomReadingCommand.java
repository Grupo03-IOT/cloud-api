package com.pe.cloudapi.monitoring.domain.model.commands;

import com.pe.cloudapi.monitoring.domain.model.valueobjects.AcousticMetrics;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThermalComfort;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registra el agregado de un minuto que sube el Edge.
 *
 * <p>Todos los value objects llegan ya calculados; este comando no computa
 * nada, solo transporta.
 */
public record RecordRoomReadingCommand(
        UUID roomId,
        OffsetDateTime ts,
        int periodS,
        AcousticMetrics acoustic,
        Climate climate,
        ThermalComfort comfort,
        Occupancy occupancy,
        DataQuality quality
) {}
