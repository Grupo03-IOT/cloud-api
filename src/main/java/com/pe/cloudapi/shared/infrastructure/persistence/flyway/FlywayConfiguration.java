package com.pe.cloudapi.shared.infrastructure.persistence.flyway;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Una migración de Flyway por bounded context.
 */
@Slf4j
@Configuration
public class FlywayConfiguration {

    private static final String MIGRATION_ROOT = "db/migration/";
    private static final String DISCOVERY_PATTERN = "classpath*:" + MIGRATION_ROOT + "*/*.sql";

    /**
     * Reemplaza la migración única de Spring Boot por una por contexto.
     *
     * <p>La instancia que Spring Boot construye se ignora a propósito: de ella
     * solo interesa el momento en que se dispara, no lo que migraría.
     *
     * @param dataSource fuente de datos de la aplicación
     * @param resolver   con el que se descubren las carpetas de migraciones
     * @return la estrategia que migra todos los contextos encontrados
     */
    @Bean
    public FlywayMigrationStrategy boundedContextMigrationStrategy(DataSource dataSource,
                                                                   ResourcePatternResolver resolver) {
        return autoConfigured -> {
            Set<String> contexts = discoverContexts(resolver);
            if (contexts.isEmpty()) {
                log.warn("No se encontró ninguna carpeta de migraciones bajo {}", MIGRATION_ROOT);
                return;
            }
            log.info("Migrando bounded contexts: {}", contexts);
            contexts.forEach(context -> migrate(dataSource, context));
        };
    }

    /**
     * Nombres de carpeta bajo {@code db/migration} que contienen migraciones.
     *
     * <p>Se ordenan alfabéticamente solo para que el registro sea estable: el
     * orden entre contextos es indiferente, porque ninguno referencia tablas de
     * otro. Si algún día importara, sería señal de que la separación se rompió.
     */
    private Set<String> discoverContexts(ResourcePatternResolver resolver) {
        Set<String> contexts = new TreeSet<>();
        try {
            for (Resource resource : resolver.getResources(DISCOVERY_PATTERN)) {
                String url = resource.getURL().toString();
                int start = url.lastIndexOf(MIGRATION_ROOT);
                if (start < 0) {
                    continue;
                }
                String tail = url.substring(start + MIGRATION_ROOT.length());
                int end = tail.indexOf('/');
                if (end > 0) {
                    contexts.add(tail.substring(0, end));
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudieron leer las migraciones", ex);
        }
        return contexts;
    }

    private void migrate(DataSource dataSource, String context) {
        Flyway.configure()
                .dataSource(dataSource)
                .schemas(context)
                .defaultSchema(context)
                .locations(locationOf(context))
                .createSchemas(true)
                .load()
                .migrate();
    }

    /** Dónde vive la carpeta de migraciones de un contexto. */
    private static String locationOf(String context) {
        return "classpath:" + MIGRATION_ROOT + context;
    }
}
