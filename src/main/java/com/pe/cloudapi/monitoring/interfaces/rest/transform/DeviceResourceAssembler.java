package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.application.internal.results.DeviceStatus;
import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DeviceResource;

import org.springframework.stereotype.Component;

/**
 * Traduce dispositivos del dominio a su representación REST.
 */
@Component
public class DeviceResourceAssembler {

    public DeviceResource toResource(DeviceStatus status) {
        Device device = status.device();
        return new DeviceResource(
                device.getId(),
                device.getCode(),
                device.getRoomId(),
                device.getFwVersion(),
                device.getLastSeen(),
                device.getLastSeq(),
                device.getLostBatches(),
                status.silent());
    }
}
