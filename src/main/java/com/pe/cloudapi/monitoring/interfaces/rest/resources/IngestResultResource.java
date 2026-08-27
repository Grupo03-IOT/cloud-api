package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado de una ingesta.
 *
 * <p>{@code updated} distinto de cero es normal, no un error: el Edge entrega
 * <em>at-least-once</em> y reintenta cuando no confirma la subida, así que el
 * mismo minuto puede llegar más de una vez.
 */
@Schema(description = "Resultado de una ingesta de lecturas")
public record IngestResultResource(

        @Schema(description = "Lecturas recibidas en la petición") int accepted,
        @Schema(description = "Minutos nuevos guardados") int inserted,
        @Schema(description = "Minutos que ya existían y se actualizaron") int updated

) {}
