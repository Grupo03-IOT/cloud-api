package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.GatewaySnapshot;

import java.util.List;

/**
 * Puerto de entrada: los Edges y su estado ahora.
 */
public interface ListGatewaysUseCase {

    List<GatewaySnapshot> execute();
}
