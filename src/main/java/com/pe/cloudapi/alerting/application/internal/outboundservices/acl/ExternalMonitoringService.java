package com.pe.cloudapi.alerting.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.model.valueobjects.RoomProfile;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;
import com.pe.cloudapi.alerting.domain.ports.out.RoomProfileProvider;
import com.pe.cloudapi.monitoring.interfaces.acl.MonitoringContextFacade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Capa anticorrupción entre {@code alerting} y {@code monitoring}.
 *
 * <p>Único punto del contexto que conoce al otro: traduce sus salas a
 * {@link RoomProfile} y a lo que pide {@link RoomDirectory}, que es lo único
 * que aquí significa algo.
 */
@Service
@RequiredArgsConstructor
public class ExternalMonitoringService implements RoomProfileProvider, RoomDirectory {

    private final MonitoringContextFacade monitoring;

    @Override
    public List<RoomProfile> rooms() {
        return monitoring.rooms().stream()
                .map(snapshot -> new RoomProfile(
                        snapshot.room().getCode(),
                        snapshot.room().getRoomTypeId()))
                .toList();
    }

    @Override
    public UUID roomIdOf(String siteCode, String roomCode) {
        return monitoring.roomIdByCode(siteCode, roomCode);
    }

    @Override
    public Map<UUID, String> roomCodes() {
        return monitoring.rooms().stream()
                .collect(Collectors.toMap(
                        snapshot -> snapshot.room().getId(),
                        snapshot -> snapshot.room().getCode(),
                        (first, second) -> first));
    }
}
