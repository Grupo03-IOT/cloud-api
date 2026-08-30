package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateRoomTypeUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomTypeCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dar de alta un tipo de sala dentro de un local.
 */
@Service
@RequiredArgsConstructor
public class CreateRoomTypeUseCaseImpl implements CreateRoomTypeUseCase {

    private final RoomTypeRepository roomTypes;
    private final SiteRepository sites;

    @Override
    @Transactional
    public RoomType execute(CreateRoomTypeCommand command) {
        if (sites.findById(command.siteId()).isEmpty()) {
            throw MonitoringError.SITE_NOT_FOUND.with(command.siteId());
        }
        roomTypes.findBySiteIdAndCode(command.siteId(), command.code()).ifPresent(existing -> {
            throw MonitoringError.ROOM_TYPE_CODE_ALREADY_USED.with(command.siteId(), command.code());
        });
        return roomTypes.save(new RoomType(command));
    }
}
