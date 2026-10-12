package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSiteRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListSiteRoomsQuery;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listar las salas activas de un local, cada una con su tipo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListSiteRoomsUseCaseImpl implements ListSiteRoomsUseCase {

    private final SiteRepository sites;
    private final RoomRepository rooms;
    private final RoomTypeRepository roomTypes;

    @Override
    public List<RoomContext> execute(ListSiteRoomsQuery query) {
        Site site = sites.findById(query.siteId())
                .orElseThrow(() -> MonitoringError.SITE_NOT_FOUND.with(query.siteId()));
        Map<UUID, RoomType> typesById = roomTypes.findAllBySiteId(site.getId()).stream()
                .collect(Collectors.toMap(RoomType::getId, Function.identity()));
        return rooms.findActiveBySiteId(site.getId()).stream()
                .map(room -> new RoomContext(room, site,
                        Optional.ofNullable(room.getRoomTypeId()).map(typesById::get)))
                .toList();
    }
}
