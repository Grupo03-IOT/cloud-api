package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSitesUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
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
public class ListSitesUseCaseImpl implements ListSitesUseCase {

    private final SiteRepository sites;

    @Override
    public List<Site> execute() {
        return sites.findAll();
    }
}
