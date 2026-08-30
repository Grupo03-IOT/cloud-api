package com.pe.cloudapi.alerting.domain.ports.out;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;

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
}
