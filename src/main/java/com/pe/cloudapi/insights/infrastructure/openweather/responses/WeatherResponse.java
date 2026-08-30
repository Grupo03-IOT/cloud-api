package com.pe.cloudapi.insights.infrastructure.openweather.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Elemento del bloque {@code weather} de la respuesta de OpenWeather.
 *
 * @param main descripción corta del estado del cielo: {@code Clear}, {@code Rain}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherResponse(String main) {}
