package com.pe.cloudapi.insights.infrastructure.configuration;

import com.pe.cloudapi.insights.domain.model.errors.InsightsError;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalogSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica el catálogo de errores de {@code insights}.
 *
 * <p>Sin esto, los códigos de este contexto escritos en una anotación de
 * validación no se podrían resolver. Es el peaje de que {@code shared} no
 * conozca a nadie.
 */
@Configuration
public class InsightsErrorCatalogConfiguration {

    @Bean
    public ErrorCatalogSource insightsErrors() {
        return InsightsError::values;
    }
}
