package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de un local.
 *
 * <p>{@code timezone} puede omitirse; el dominio asume la de Lima, que es donde
 * está el único local por ahora.
 */
@Schema(description = "New site")
public record CreateSiteResource(

        @NotBlank @Size(max = 64)
        @Schema(description = "Identificador estable y legible", example = "coworking-lima-centro")
        String code,

        @NotBlank @Size(max = 128)
        String name,

        @Size(max = 256)
        String address,

        @Size(max = 64)
        @Schema(example = "America/Lima")
        String timezone
) {}
