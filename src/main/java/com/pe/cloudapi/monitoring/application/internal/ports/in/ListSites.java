package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;

import java.util.List;

/**
 * Puerto de entrada: listar los locales.
 */
public interface ListSites {

    List<Site> execute();
}
