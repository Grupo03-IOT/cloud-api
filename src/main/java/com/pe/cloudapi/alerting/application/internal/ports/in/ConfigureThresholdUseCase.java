package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.domain.model.commands.ConfigureThresholdCommand;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;

/**
 * Puerto de entrada: fijar el umbral de una métrica para un tipo de sala.
 *
 * <p>Es idempotente: si ya existe uno para esa combinación, se reajusta.
 */
public interface ConfigureThresholdUseCase {

    Threshold execute(ConfigureThresholdCommand command);
}
