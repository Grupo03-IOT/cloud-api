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
 * Una migración de Flyway por bounded context, cada una con su esquema y su
 * propia tabla de historial: por eso todos empiezan por {@code V1}.
 *
 * <p>Los contextos se descubren leyendo las carpetas bajo {@code db/migration};
 * no hay lista que mantener. <strong>El nombre de la carpeta es el nombre del
 * esquema.</strong>
 */
@Slf4j
@Configuration
public class FlywayConfiguration {

    private static final String MIGRATION_ROOT = "db/migration/";

    /** {@code classpath*:} recorre todas las entradas, no solo la primera. */
    private static final String DISCOVERY_PATTERN = "classpath*:" + MIGRATION_ROOT + "*/*.sql";

    /**
     * Sustituye la migración única de Spring Boot por una por contexto.
     *
     * <p>Se sustituye la estrategia en vez de desactivar la autoconfiguración
     * porque Hibernate depende de su bean para no validar el esquema antes de
     * que las migraciones corran.
     *
     * @param dataSource fuente de datos de la aplicación
     * @param resolver   con el que se descubren las carpetas
     * @return la estrategia que migra cada contexto encontrado
     */
    @Bean
    public FlywayMigrationStrategy boundedContextMigrationStrategy(DataSource dataSource,
                                                                   ResourcePatternResolver resolver) {
        return autoConfigured -> {
            Set<String> contexts = discoverContexts(resolver);
            if (contexts.isEmpty()) {
                log.warn("Sin carpetas de migración bajo {}", MIGRATION_ROOT);
                return;
            }
            log.info("Migrando bounded contexts: {}", contexts);
            contexts.forEach(context -> migrate(dataSource, context));
        };
    }

    /**
     * Carpetas con migraciones bajo {@code db/migration}.
     *
     * <p>Ordenadas solo para que el registro sea estable: el orden entre
     * contextos es indiferente porque ninguno referencia tablas de otro.
     *
     * @param resolver resolutor de recursos del classpath
     * @return los nombres de contexto encontrados
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

    /**
     * Migra un contexto contra su propio esquema.
     *
     * @param dataSource fuente de datos
     * @param context    nombre del contexto, que es también el del esquema
     */
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

    private static String locationOf(String context) {
        return "classpath:" + MIGRATION_ROOT + context;
    }
}
