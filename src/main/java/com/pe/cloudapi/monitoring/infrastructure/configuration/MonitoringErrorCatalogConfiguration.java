package com.pe.cloudapi.monitoring.infrastructure.configuration;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalogSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el catálogo de errores de {@code monitoring}.
 *
 * <p>Sin esto, los códigos de este contexto escritos en una anotación de
 * validación no se podrían resolver. Es el peaje de que {@code shared} no
 * conozca a nadie.
 */
@Configuration
public class MonitoringErrorCatalogConfiguration {

    @Bean
    public ErrorCatalogSource monitoringErrors() {
        return MonitoringError::values;
    }
}
