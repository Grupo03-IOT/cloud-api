package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Una sala instrumentada, con un resumen de su última lectura.
 *
 * <p>{@code latest} viene nulo mientras la sala no haya recibido ninguna
 * lectura, lo que ocurre entre que se da de alta automáticamente y llega su
 * primer minuto.
 */
@Schema(description = "Instrumented room with a summary of its latest reading")
public record RoomResource(

        UUID id,

        @Schema(description = "Código configurado en el firmware", example = "sala-01")
        String code,

        String displayName,
        String floor,
        Integer capacity,
        Float areaM2,
        boolean active,

        @Schema(description = "Falso mientras un administrador no le asigne tipo; "
                + "sin tipo no tiene umbrales aplicables")
        boolean classified,

        Latest latest

) {

    /**
     * Resumen de la última lectura.
     *
     * <p>{@code ageSeconds} es la <strong>frescura del dato</strong>: cuántos
     * segundos han pasado desde el minuto que describe. Se devuelve el número
     * en bruto a propósito, sin traducirlo a un estado tipo «en línea» o
     * «caído»: quién consume decide qué es demasiado viejo para su caso. Una
     * app que elige sala tolera cinco minutos; un panel de mantenimiento, no.
     */
    @Schema(description = "Summary of the latest reading")
    public record Latest(

            @Schema(description = "Minuto que describe la lectura")
            OffsetDateTime ts,

            @Schema(description = "Antigüedad del dato en segundos", example = "95")
            long ageSeconds,

            @Schema(description = "Nivel sonoro equivalente en dBA") Float laeq,
            Float tempC,
            Float rhPct,
            @Schema(description = "Porcentaje de insatisfechos, ISO 7730") Float ppd,
            @Schema(example = "neutral") String thermalVerdict,
            Float occupiedPct,

            @Schema(description = "Falso si el minuto se construyó con menos "
                    + "lotes de los esperados")
            boolean complete
    ) {}
}
