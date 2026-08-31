package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Los umbrales vigentes para una sala.
 *
 * @param roomCode   tal como lo reporta el dispositivo
 * @param thresholds vacío si la sala no está clasificada: quien lo reciba
 *                   aplicará sus propios valores por defecto
 */
@Schema(description = "Thresholds in force for a room")
public record RoomThresholdsResource(

        @Schema(example = "booth-01")
        String roomCode,

        List<ThresholdResource> thresholds
) {}
