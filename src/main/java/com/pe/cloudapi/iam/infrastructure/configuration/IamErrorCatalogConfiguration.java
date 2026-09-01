package com.pe.cloudapi.iam.infrastructure.configuration;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalogSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamErrorCatalogConfiguration {

    @Bean
    public ErrorCatalogSource iamErrors() {
        return IamError::values;
    }
}
