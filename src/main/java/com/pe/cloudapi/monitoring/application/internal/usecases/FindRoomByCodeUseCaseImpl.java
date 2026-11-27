package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.FindRoomByCodeUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.FindRoomByCodeQuery;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Resolver una sala por el código que trae el Edge.
 *
 * <p>Resuelve el local igual que la ingesta de lecturas: por su código si
 * viene, o el único registrado si no. A diferencia de la ingesta, una sala
 * desconocida <strong>no</strong> se da de alta: quien pregunta solo quiere
 * referirse a una que ya existe.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindRoomByCodeUseCaseImpl implements FindRoomByCodeUseCase {

    private final SiteRepository sites;
    private final RoomRepository rooms;

    @Override
    public Room execute(FindRoomByCodeQuery query) {
        UUID siteId = resolveSite(query.siteCode());
        return rooms.findBySiteIdAndCode(siteId, query.roomCode())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(query.roomCode()));
    }

    private UUID resolveSite(String siteCode) {
        if (siteCode != null && !siteCode.isBlank()) {
            return sites.findByCode(siteCode).map(Site::getId)
                    .orElseThrow(() -> MonitoringError.SITE_NOT_FOUND.with(siteCode));
        }
        var available = sites.findAll();
        if (available.isEmpty()) throw MonitoringError.NO_SITE_AVAILABLE.with();
        if (available.size() > 1) throw MonitoringError.SITE_CODE_REQUIRED.with();
        return available.getFirst().getId();
    }
}
