package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDevicesUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.DeviceStatus;
import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDevicesQuery;
import com.pe.cloudapi.monitoring.domain.ports.out.DeviceRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Listar los dispositivos con su estado, derivado al consultar de su último lote.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListDevicesUseCaseImpl implements ListDevicesUseCase {

    private final DeviceRepository devices;

    @Override
    public List<DeviceStatus> execute(ListDevicesQuery query) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<Device> found = query.roomId() == null
                ? devices.findAll()
                : devices.findAllByRoomId(query.roomId());
        return found.stream()
                .map(device -> new DeviceStatus(device, device.isSilentAt(now)))
                .toList();
    }
}
