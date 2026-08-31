package com.pe.cloudapi.monitoring.interfaces.acl;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetReadingsInRangeUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;

import java.util.List;
import com.pe.cloudapi.monitoring.domain.model.queries.GetReadingsInRangeQuery;
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

    @Override
    public RoomReadings readingsInRange(UUID roomId, OffsetDateTime from, OffsetDateTime to) {
        return getReadingsInRange.execute(new GetReadingsInRangeQuery(roomId, from, to));
    }

    @Override
    public List<RoomSnapshot> rooms() {
        return listRooms.execute();
    }
}
