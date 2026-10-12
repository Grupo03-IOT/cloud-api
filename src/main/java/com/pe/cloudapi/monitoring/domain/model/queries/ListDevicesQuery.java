package com.pe.cloudapi.monitoring.domain.model.queries;

import java.util.UUID;

/**
 * Los dispositivos, todos o los de una sala.
 *
 * @param roomId nulo para todos
 */
public record ListDevicesQuery(UUID roomId) {
}
