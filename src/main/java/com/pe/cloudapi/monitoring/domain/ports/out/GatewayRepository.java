package com.pe.cloudapi.monitoring.domain.ports.out;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Gateway;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de {@link Gateway}.
 */
public interface GatewayRepository {

    Gateway save(Gateway gateway);

    Optional<Gateway> findByCode(String code);

    /** Ordenados por código. */
    List<Gateway> findAll();
}
