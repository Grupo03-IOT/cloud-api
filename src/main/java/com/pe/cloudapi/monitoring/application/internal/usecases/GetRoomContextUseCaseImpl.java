package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetRoomContextUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Obtener una sala con su local y su tipo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetRoomContextUseCaseImpl implements GetRoomContextUseCase {

    private final RoomRepository rooms;
    private final SiteRepository sites;
    private final RoomTypeRepository roomTypes;

    @Override
    public RoomContext execute(GetRoomQuery query) {
        Room room = rooms.findById(query.roomId())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(query.roomId()));
        Site site = sites.findById(room.getSiteId())
                .orElseThrow(() -> MonitoringError.SITE_NOT_FOUND.with(room.getSiteId()));
        return new RoomContext(room, site,
                Optional.ofNullable(room.getRoomTypeId()).flatMap(roomTypes::findById));
    }
}
