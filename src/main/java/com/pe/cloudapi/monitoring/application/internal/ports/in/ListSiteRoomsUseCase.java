package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomContext;
import com.pe.cloudapi.monitoring.domain.model.queries.ListSiteRoomsQuery;

import java.util.List;

/**
 * Puerto de entrada: las salas activas de un local, cada una con su tipo.
 */
public interface ListSiteRoomsUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND} si el local no existe
     */
    List<RoomContext> execute(ListSiteRoomsQuery query);
}
