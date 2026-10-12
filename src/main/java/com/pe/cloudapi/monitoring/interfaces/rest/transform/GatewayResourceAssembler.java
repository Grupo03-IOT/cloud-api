package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.application.internal.results.GatewaySnapshot;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.GatewayResource;

import org.springframework.stereotype.Component;

/**
 * Traduce Edges del dominio a su representación REST.
 */
@Component
public class GatewayResourceAssembler {

    public GatewayResource toResource(GatewaySnapshot snapshot) {
        Gateway gateway = snapshot.gateway();
        return new GatewayResource(
                gateway.getId(),
                gateway.getCode(),
                gateway.getSiteId(),
                gateway.getVersion(),
                gateway.getLastSeen(),
                gateway.getPendingUpload(),
                gateway.getRoomsReported(),
                snapshot.status().toCode());
    }
}
