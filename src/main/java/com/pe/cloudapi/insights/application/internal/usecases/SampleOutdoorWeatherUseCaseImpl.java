package com.pe.cloudapi.insights.application.internal.usecases;

import com.pe.cloudapi.insights.application.internal.ports.in.SampleOutdoorWeatherUseCase;
import com.pe.cloudapi.insights.domain.ports.out.WeatherObservationRepository;
import com.pe.cloudapi.insights.domain.ports.out.OutdoorWeatherProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tomar una muestra del tiempo exterior y guardarla.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SampleOutdoorWeatherUseCaseImpl implements SampleOutdoorWeatherUseCase {

    private final OutdoorWeatherProvider provider;
    private final WeatherObservationRepository weatherObservations;

    @Override
    @Transactional
    public boolean execute() {
        return provider.fetchCurrent()
                .map(observation -> {
                    weatherObservations.save(observation);
                    log.debug("Exterior: {} °C a las {}",
                            observation.getTempC(), observation.getObservedAt());
                    return true;
                })
                .orElse(false);
    }
}
