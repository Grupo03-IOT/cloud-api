package com.pe.cloudapi.insights.infrastructure.openweather.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Respuesta de {@code GET /weather} de OpenWeather.
 *
 * <p>Recortada a lo que este contexto usa. {@code @JsonIgnoreProperties} no es
 * opcional: la respuesta real trae coordenadas, viento, nubosidad, visibilidad
 * y una decena de campos más, y sin ignorarlos la deserialización fallaría.
 *
 * @param main    bloque con temperatura y humedad
 * @param weather descripciones del estado del cielo; se usa la primera
 * @param dt      instante de la medición, en segundos desde la época Unix
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentWeatherResponse(
        MainResponse main,
        List<WeatherResponse> weather,
        long dt
) {}
