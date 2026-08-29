package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;

/**
 * Puerto de entrada: dar de alta un local.
 */
public interface CreateSite {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_CODE_ALREADY_USED} si el código está tomado
     */
    Site execute(CreateSiteCommand command);
}
