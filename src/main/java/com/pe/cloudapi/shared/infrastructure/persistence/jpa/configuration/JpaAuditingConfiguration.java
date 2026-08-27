package com.pe.cloudapi.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Activa la auditoría de Spring Data JPA.
 *
 * <p>El proveedor de fechas fuerza UTC a propósito: toda la telemetría viaja en
 * UTC desde el ESP32, y mezclar horas locales en las columnas de auditoría
 * produce desfases muy difíciles de depurar después.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "utcDateTimeProvider")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return Optional::empty;
    }

    @Bean
    public DateTimeProvider utcDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
