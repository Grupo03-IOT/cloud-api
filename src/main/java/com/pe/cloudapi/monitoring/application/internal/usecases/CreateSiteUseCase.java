package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateSite;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.repositories.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dar de alta un local.
 *
 * <p>El código se comprueba aquí porque la base no tiene restricción
 * {@code UNIQUE}: es deuda técnica registrada, y mientras dure la unicidad
 * depende de esta comprobación.
 */
@Service
@RequiredArgsConstructor
public class CreateSiteUseCase implements CreateSite {

    private final SiteRepository sites;

    @Override
    @Transactional
    public Site execute(CreateSiteCommand command) {
        sites.findByCode(command.code()).ifPresent(existing -> {
            throw MonitoringError.SITE_CODE_ALREADY_USED.with(command.code());
        });
        return sites.save(new Site(command));
    }
}
