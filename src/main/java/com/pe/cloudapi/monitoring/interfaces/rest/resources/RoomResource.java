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
     * <p>La frescura se deduce de {@code ts}: es el minuto que la lectura
     * describe, y comparándolo con la hora actual el cliente sabe cuán vieja
     * es. No se envía la antigüedad ya calculada porque envejecería en cuanto
     * saliera de aquí, mientras que {@code ts} es un hecho que no caduca.
     */
    @Schema(description = "Summary of the latest reading")
    public record Latest(

            @Schema(description = "Minuto que describe la lectura, en UTC")
            OffsetDateTime ts,

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
