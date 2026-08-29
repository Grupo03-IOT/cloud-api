package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de un tipo de sala.
 *
 * <p>El tipo es lo que da sentido a los umbrales: lo aceptable en una zona
 * común es inaceptable en una cabina de llamadas.
 */
@Schema(description = "New room type")
public record CreateRoomTypeResource(

        @NotBlank @Size(max = 32)
        @Schema(example = "call_booth")
        String code,

        @NotBlank @Size(max = 128)
        @Schema(example = "Call booth")
        String displayName,

        @Size(max = 256)
        String description
) {}
