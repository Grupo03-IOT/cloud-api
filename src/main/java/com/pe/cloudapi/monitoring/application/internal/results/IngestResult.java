package com.pe.cloudapi.monitoring.application.internal.results;

/**
 * Qué ocurrió al ingerir un lote.
 *
 * <p>{@code updated} distinto de cero es normal, no un error: el Edge entrega
 * <em>at-least-once</em> y reintenta cuando no confirma la subida, así que el
 * mismo minuto puede llegar más de una vez.
 *
 * @param accepted lecturas recibidas
 * @param inserted minutos nuevos guardados
 * @param updated  minutos que ya existían
 */
public record IngestResult(int accepted, int inserted, int updated) {
}
