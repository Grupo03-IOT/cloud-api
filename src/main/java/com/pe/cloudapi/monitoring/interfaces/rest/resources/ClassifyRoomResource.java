package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Asignación de tipo a una sala.
 */
@Schema(description = "Room classification")
public record ClassifyRoomResource(

        @NotNull
        @Schema(description = "Tipo a asignar. Debe pertenecer al mismo local que la sala")
        UUID roomTypeId
) {}
