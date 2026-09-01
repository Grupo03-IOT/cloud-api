package com.pe.cloudapi.alerting.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.model.valueobjects.RoomProfile;
import com.pe.cloudapi.alerting.domain.ports.out.RoomProfileProvider;
import com.pe.cloudapi.monitoring.interfaces.acl.MonitoringContextFacade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Capa anticorrupción entre {@code alerting} y {@code monitoring}.
 *
 * <p>Único punto del contexto que conoce al otro: traduce sus salas a
 * {@link RoomProfile}, que es lo único que aquí significa algo.
 */
@Service
@RequiredArgsConstructor
public class ExternalMonitoringService implements RoomProfileProvider {

    private final MonitoringContextFacade monitoring;

    @Override
    public List<RoomProfile> rooms() {
        return monitoring.rooms().stream()
                .map(snapshot -> new RoomProfile(
                        snapshot.room().getCode(),
                        snapshot.room().getRoomTypeId()))
                .toList();
    }
}
