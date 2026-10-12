package com.pe.cloudapi.monitoring.interfaces.acl;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetReadingsInRangeUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.GetRoomContextUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSiteRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;

import java.util.List;
import com.pe.cloudapi.monitoring.domain.model.queries.GetReadingsInRangeQuery;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;
import com.pe.cloudapi.monitoring.domain.model.queries.ListSiteRoomsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Delega en los casos de uso de {@code monitoring}, nunca en sus repositorios.
 */
@Service
@RequiredArgsConstructor
public class MonitoringContextFacadeImpl implements MonitoringContextFacade {

    private final GetReadingsInRangeUseCase getReadingsInRange;
    private final ListRoomsUseCase listRooms;
    private final GetRoomContextUseCase getRoomContext;
    private final ListSiteRoomsUseCase listSiteRooms;

    @Override
    public RoomReadings readingsInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return getReadingsInRange.execute(new GetReadingsInRangeQuery(roomId, from, to));
    }

    @Override
    public List<RoomSnapshot> rooms() {
        return listRooms.execute();
    }

    @Override
    public RoomContext room(UUID roomId) {
        return getRoomContext.execute(new GetRoomQuery(roomId));
    }

    @Override
    public List<RoomContext> roomsOfSite(UUID siteId) {
        return listSiteRooms.execute(new ListSiteRoomsQuery(siteId));
    }
}
