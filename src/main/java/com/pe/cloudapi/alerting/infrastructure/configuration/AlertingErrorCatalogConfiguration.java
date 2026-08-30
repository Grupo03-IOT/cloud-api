package com.pe.cloudapi.alerting.infrastructure.configuration;

import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalogSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el catálogo de errores de {@code alerting}.
 *
 * <p>Sin esto, los códigos de este contexto escritos en una anotación de
 * validación no se podrían resolver. Es el peaje de que {@code shared} no
 * conozca a nadie.
 */
@Configuration
public class AlertingErrorCatalogConfiguration {

    @Bean
    public ErrorCatalogSource alertingErrors() {
        return AlertingError::values;
    }
}
