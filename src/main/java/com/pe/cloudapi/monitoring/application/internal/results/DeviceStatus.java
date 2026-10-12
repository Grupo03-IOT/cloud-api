package com.pe.cloudapi.monitoring.application.internal.results;

import com.pe.cloudapi.monitoring.domain.model.entities.Device;

/**
 * Un dispositivo y si está callado en el momento de la consulta.
 */
public record DeviceStatus(Device device, boolean silent) {
}
