package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.RecordHeartbeatUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.RecordHeartbeatCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.ports.out.GatewayRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Registrar el latido de un Edge, dándolo de alta la primera vez.
 *
 * <p>La hora es la de llegada al cloud, no la que diga el Edge: es el cloud
 * quien decide si lleva demasiado tiempo callado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RecordHeartbeatUseCaseImpl implements RecordHeartbeatUseCase {

    private final SiteRepository sites;
    private final GatewayRepository gateways;

    @Override
    public void execute(String code, String siteCode, String version, int pendingUpload, int roomsReported) {
        Site site = sites.findByCode(siteCode)
                .orElseThrow(() -> MonitoringError.SITE_NOT_FOUND.with(siteCode));
        RecordHeartbeatCommand command = new RecordHeartbeatCommand(code, site.getId(), version,
                pendingUpload, roomsReported, OffsetDateTime.now(ZoneOffset.UTC));

        Gateway gateway = gateways.findByCode(code)
                .map(existing -> {
                    existing.handle(command);
                    return existing;
                })
                .orElseGet(() -> new Gateway(command));
        gateways.save(gateway);
    }
}
