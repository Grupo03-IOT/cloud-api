package com.pe.cloudapi.alerting.domain.ports.out;

import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de {@link Alert}.
 */
public interface AlertRepository {

    Alert save(Alert alert);

    Optional<Alert> findById(UUID id);

    /**
     * Clave de idempotencia: el Edge reintenta los eventos que no confirmó.
     */
    Optional<Alert> findBySourceKey(String sourceKey);

    /**
     * @return las alertas que cumplen los filtros, de la apertura más reciente a la más antigua
     */
    List<Alert> search(ListAlertsQuery query);
}
