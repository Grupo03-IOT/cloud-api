package com.pe.cloudapi.shared.interfaces.rest;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Qué orígenes pueden llamar a la API desde un navegador.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfiguration(@Value("${app.cors.allowed-origins:}") String origins) {
        this.allowedOrigins = origins == null || origins.isBlank()
                ? new String[0]
                : origins.split("\\s*,\\s*");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (allowedOrigins.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
