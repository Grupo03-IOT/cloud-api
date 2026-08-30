package com.pe.cloudapi.alerting.domain.model.queries;

import java.util.Objects;
import java.util.UUID;

/**
 * Umbrales configurados para un tipo de sala.
 *
 * @param roomTypeId tipo de sala consultado
 */
public record ListThresholdsQuery(UUID roomTypeId) {

    public ListThresholdsQuery {
        Objects.requireNonNull(roomTypeId, "roomTypeId");
    }
}
