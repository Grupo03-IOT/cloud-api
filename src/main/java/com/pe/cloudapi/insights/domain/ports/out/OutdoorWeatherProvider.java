package com.pe.cloudapi.insights.domain.ports.out;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;

import java.util.Optional;

/**
 * De dónde sale el tiempo en el exterior.
 *
 * <p>Puerto de salida: este contexto declara que necesita las condiciones de
 * fuera, sin saber que las sirve OpenWeather. Cambiar de proveedor es cambiar
 * la implementación.
 */
public interface OutdoorWeatherProvider {

    /**
     * @return las condiciones actuales, o vacío si el proveedor no responde
     */
    Optional<WeatherObservation> fetchCurrent();
}
