package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.time.ZoneId;
import java.util.UUID;

/**
 * Lo que este contexto necesita saber de una sala.
 *
 * @param roomTypeId   nulo mientras no esté clasificada
 * @param roomTypeCode nulo mientras no esté clasificada
 * @param zone         la zona horaria de su local
 */
public record AnalyzedRoom(UUID id, String code, UUID roomTypeId, String roomTypeCode, ZoneId zone) {

    public boolean isClassified() {
        return roomTypeId != null;
    }
}
