package com.pe.cloudapi.insights.infrastructure.openweather.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bloque {@code main} de la respuesta de OpenWeather.
 *
 * <p>La temperatura llega en grados Celsius porque la integración pide
 * {@code units=metric}, configurado en {@code application.yaml}.
 *
 * @param temp     temperatura exterior
 * @param humidity humedad relativa en porcentaje
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MainResponse(Float temp, Float humidity) {}
