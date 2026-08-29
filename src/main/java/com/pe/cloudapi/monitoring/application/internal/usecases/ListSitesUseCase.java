package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSites;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.repositories.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar los locales.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListSitesUseCase implements ListSites {

    private final SiteRepository sites;

    @Override
    public List<Site> execute() {
        return sites.findAll();
    }
}
