package com.pe.cloudapi.support;

import com.pe.cloudapi.alerting.infrastructure.configuration.AlertingErrorCatalogConfiguration;
import com.pe.cloudapi.iam.infrastructure.authentication.ApiKeyAuthenticationFilter;
import com.pe.cloudapi.iam.infrastructure.authentication.JwtConfiguration;
import com.pe.cloudapi.iam.infrastructure.authentication.SecurityConfiguration;
import com.pe.cloudapi.iam.infrastructure.configuration.IamErrorCatalogConfiguration;
import com.pe.cloudapi.monitoring.infrastructure.configuration.MonitoringErrorCatalogConfiguration;
import com.pe.cloudapi.shared.interfaces.rest.ErrorCatalogs;
import com.pe.cloudapi.shared.interfaces.rest.GlobalExceptionHandler;
import com.pe.cloudapi.shared.interfaces.rest.SecurityErrorResponder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Web-layer slice shared by the controller and acceptance tests.
 *
 * <p>It brings the production security chain (JWT resource server, API key
 * filter and method security), the global error handler and the error
 * catalogs, but none of the persistence or Feign infrastructure, so the tests
 * run without a database or network.
 *
 * <p>The API key filter only runs inside the security chain. Left as a plain
 * servlet filter, MockMvc would run it first and the security chain would then
 * reset the context it populated.
 *
 * <p>It is deliberately a plain class and not a {@code @Configuration}: that
 * keeps the application's component scan from picking it up.
 */
@Import({
        SecurityConfiguration.class,
        JwtConfiguration.class,
        ApiKeyAuthenticationFilter.class,
        SecurityErrorResponder.class,
        GlobalExceptionHandler.class,
        ErrorCatalogs.class,
        IamErrorCatalogConfiguration.class,
        MonitoringErrorCatalogConfiguration.class,
        AlertingErrorCatalogConfiguration.class
})
@EnableWebSecurity
public class WebLayerTestConfiguration {

    @Bean
    FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyFilterOnlyInSecurityChain(
            ApiKeyAuthenticationFilter filter) {
        FilterRegistrationBean<ApiKeyAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
