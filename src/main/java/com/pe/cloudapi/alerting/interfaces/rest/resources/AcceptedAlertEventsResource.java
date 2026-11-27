package com.pe.cloudapi.alerting.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @param accepted eventos guardados, nuevos o repetidos
 */
@Schema(description = "Outcome of an alert event batch")
public record AcceptedAlertEventsResource(int accepted) {
}
