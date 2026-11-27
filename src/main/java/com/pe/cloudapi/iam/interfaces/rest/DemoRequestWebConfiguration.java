package com.pe.cloudapi.iam.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Aplica el límite por IP solo a las solicitudes de demostración. */
@Configuration
@RequiredArgsConstructor
public class DemoRequestWebConfiguration implements WebMvcConfigurer {

    private final DemoRequestRateLimitInterceptor rateLimit;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimit).addPathPatterns("/api/v1/demo-requests");
    }
}
