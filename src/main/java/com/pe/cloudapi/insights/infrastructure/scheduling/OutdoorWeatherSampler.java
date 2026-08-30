package com.pe.cloudapi.insights.infrastructure.scheduling;

import com.pe.cloudapi.insights.application.internal.ports.in.SampleOutdoorWeatherUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Muestrea el tiempo exterior cada diez minutos.
 *
 * <p>El plan gratuito de OpenWeather no sirve histórico: para poder correlacionar
 * un periodo hay que haberlo ido guardando. De ahí que esto exista.
 *
 * <p>Diez minutos es la cadencia con la que el proveedor actualiza sus
 * mediciones; consultar más a menudo devolvería el mismo dato y gastaría cuota.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutdoorWeatherSampler {

    private final SampleOutdoorWeatherUseCase sampleOutdoorWeather;

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT30S")
    public void sample() {
        try {
            sampleOutdoorWeather.execute();
        } catch (RuntimeException ex) {
            log.warn("Fallo al muestrear el exterior: {}", ex.getMessage());
        }
    }
}
