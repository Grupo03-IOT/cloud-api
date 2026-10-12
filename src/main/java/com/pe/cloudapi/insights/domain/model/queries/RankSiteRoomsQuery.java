package com.pe.cloudapi.insights.domain.model.queries;

import com.pe.cloudapi.insights.domain.model.valueobjects.Period;

import java.util.Objects;
import java.util.UUID;

/**
 * Comparativa de las salas de un local en un periodo.
 */
public record RankSiteRoomsQuery(UUID siteId, Period period) {

    public RankSiteRoomsQuery {
        Objects.requireNonNull(siteId, "siteId");
        Objects.requireNonNull(period, "period");
    }
}
