package com.pe.cloudapi.insights.infrastructure.openweather;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;
import com.pe.cloudapi.insights.domain.ports.out.OutdoorWeatherProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Adaptador entre el cliente de OpenWeather y el puerto del dominio.
 *
 * <p>Aquí muere el fallo: si el proveedor no responde, rechaza la clave o
 * devuelve algo inesperado, se registra y se devuelve vacío. Correlacionar con
 * el exterior mejora el análisis, pero que un servicio ajeno esté caído no
 * puede dejar al coworking sin su telemetría.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWeatherAdapter implements OutdoorWeatherProvider {

    private final OpenWeatherClient client;

    @Override
    public Optional<WeatherObservation> fetchCurrent() {
        try {
            return Optional.ofNullable(client.currentWeather()).map(response -> new WeatherObservation(
                    OffsetDateTime.ofInstant(Instant.ofEpochSecond(response.dt()), ZoneOffset.UTC),
                    response.main() == null ? null : response.main().temp(),
                    response.main() == null ? null : response.main().humidity(),
                    response.weather() == null || response.weather().isEmpty()
                            ? null : response.weather().getFirst().main()));
        } catch (RuntimeException ex) {
            log.warn("OpenWeather no respondió: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
