package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.queries.ListThresholdsQuery;

import java.util.List;

/**
 * Puerto de entrada: listar los umbrales activos de un tipo de sala.
 */
public interface ListThresholds {

    List<Threshold> execute(ListThresholdsQuery query);
}
