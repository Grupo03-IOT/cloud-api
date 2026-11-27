package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Cuántos reportes de cada motivo tiene una sala.
 */
@Schema(description = "Discomfort reports per room and category")
public record DiscomfortSummaryResource(UUID roomId, String roomCode, long noise, long warm,
                                        long cold, long total) {
}
