package com.pe.cloudapi.insights.infrastructure.openweather;

import com.pe.cloudapi.insights.infrastructure.openweather.responses.CurrentWeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cliente declarativo de OpenWeather, el servicio externo de terceros de la
 * solución.
 *
 * <p>Solo describe la llamada; la implementación la genera Feign. Todo lo demás
 * —URL base, tiempos de espera, clave, ciudad y unidades— vive en
 * {@code application.yaml} bajo
 * {@code spring.cloud.openfeign.client.config.openweather}, así que se ajusta
 * sin recompilar y sin tocar esta interfaz.
 */
@FeignClient(name = "openweather")
public interface OpenWeatherClient {

    /**
     * Condiciones actuales de la ciudad configurada.
     *
     * <p>Sin parámetros a propósito: la clave, la ciudad y las unidades son
     * constantes de la integración, y van como parámetros por defecto en la
     * configuración.
     *
     * <p>Cuando el producto atienda varios locales, la ciudad dejará de ser
     * constante y volverá a esta firma.
     *
     * @return la medición más reciente del proveedor
     */
    @GetMapping("/weather")
    CurrentWeatherResponse currentWeather();
}
