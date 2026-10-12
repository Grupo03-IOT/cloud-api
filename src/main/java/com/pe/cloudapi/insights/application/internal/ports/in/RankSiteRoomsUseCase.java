package com.pe.cloudapi.insights.application.internal.ports.in;

import com.pe.cloudapi.insights.domain.model.queries.RankSiteRoomsQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomRanking;

import java.util.List;

/**
 * Puerto de entrada: las salas de un local, de peor a mejor cumplimiento.
 */
public interface RankSiteRoomsUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND} si el local no existe
     */
    List<RoomRanking> execute(RankSiteRoomsQuery query);
}
