package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomTypes;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListRoomTypesQuery;
import com.pe.cloudapi.monitoring.domain.repositories.RoomTypeRepository;
import com.pe.cloudapi.monitoring.domain.repositories.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar los tipos de sala de un local.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRoomTypesUseCase implements ListRoomTypes {

    private final RoomTypeRepository roomTypes;
    private final SiteRepository sites;

    @Override
    public List<RoomType> execute(ListRoomTypesQuery query) {
        if (sites.findById(query.siteId()).isEmpty()) {
            throw MonitoringError.SITE_NOT_FOUND.with(query.siteId());
        }
        return roomTypes.findAllBySiteId(query.siteId());
    }
}
