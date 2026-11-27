package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Lote de eventos de alerta que sube el Edge.
 *
 * @param siteCode local de las salas; opcional mientras haya uno solo, igual
 *                 que en la ingesta de lecturas
 * @param events   eventos, al menos uno
 */
@Schema(description = "Batch of alert events from the Edge")
public record AlertEventBatchResource(

        @Schema(description = "Site of the rooms. Optional while a single site is registered",
                example = "lima-centro")
        String siteCode,

        @NotEmpty(message = "ALERTING_ALERT_BATCH_EMPTY")
        List<@Valid AlertEventResource> events
) {}
