package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.GatewayStatus;

/**
 * Un Edge y su estado en el momento de la consulta.
 */
public record GatewaySnapshot(Gateway gateway, GatewayStatus status) {
}
