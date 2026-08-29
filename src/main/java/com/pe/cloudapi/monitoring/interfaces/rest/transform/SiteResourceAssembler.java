package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.CreateSiteResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.SiteResource;
import org.springframework.stereotype.Component;

/**
 * Traduce entre los recursos REST de locales y el dominio.
 */
@Component
public class SiteResourceAssembler {

    public CreateSiteCommand toCommand(CreateSiteResource resource) {
        return new CreateSiteCommand(
                resource.code(), resource.name(), resource.address(), resource.timezone());
    }

    public SiteResource toResource(Site site) {
        return new SiteResource(
                site.getId(), site.getCode(), site.getName(),
                site.getAddress(), site.getTimezone());
    }
}
