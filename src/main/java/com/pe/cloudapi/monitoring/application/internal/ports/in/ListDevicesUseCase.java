package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.DeviceStatus;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDevicesQuery;

import java.util.List;

/**
 * Puerto de entrada: los dispositivos y si están callados ahora.
 */
public interface ListDevicesUseCase {

    List<DeviceStatus> execute(ListDevicesQuery query);
}
