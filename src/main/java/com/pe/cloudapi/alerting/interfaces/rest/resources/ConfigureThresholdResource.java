package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuración de un umbral.
 *
 * <p>La métrica y el tipo de sala vienen en la ruta: son lo que identifica al
 * umbral, no parte de su contenido.
 */
@Schema(description = "Threshold configuration")
public record ConfigureThresholdResource(

        @NotNull
        @Schema(description = "Valor a partir del cual se avisa", example = "50")
        Float warnValue,

        @Schema(description = "Valor a partir del cual la situación es crítica. "
                + "Debe ser mayor que el de aviso", example = "60")
        Float criticalValue,

        @NotNull @Positive
        @Schema(description = "Minutos seguidos que debe sostenerse para contar como "
                + "alerta. Un portazo no es un problema de ruido; veinte minutos "
                + "de gritos sí", example = "2")
        Integer sustainedMinutes,

        @NotNull
        Boolean enabled
) {}
