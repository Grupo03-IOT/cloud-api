package com.pe.cloudapi.monitoring.domain.repositories;

import com.pe.cloudapi.monitoring.domain.model.entities.Threshold;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThresholdMetric;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link Threshold}.
 */
public interface ThresholdRepository {

    Threshold save(Threshold threshold);

    List<Threshold> findEnabledByRoomTypeId(UUID roomTypeId);

    Optional<Threshold> findByRoomTypeIdAndMetric(UUID roomTypeId, ThresholdMetric metric);

    /**
     * Umbrales aplicables a una sala, resueltos a través de su tipo. Vacío si
     * la sala todavía no está clasificada.
     */
    List<Threshold> findApplicableToRoom(UUID roomId);
}
