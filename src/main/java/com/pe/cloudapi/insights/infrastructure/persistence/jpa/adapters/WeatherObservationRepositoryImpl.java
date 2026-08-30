package com.pe.cloudapi.insights.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;
import com.pe.cloudapi.insights.domain.ports.out.WeatherObservationRepository;
import com.pe.cloudapi.insights.infrastructure.persistence.jpa.entities.WeatherObservationEntity;
import com.pe.cloudapi.insights.infrastructure.persistence.jpa.repositories.WeatherObservationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Implementación de {@link WeatherObservationRepository} sobre Spring Data.
 */
@Component
@RequiredArgsConstructor
public class WeatherObservationRepositoryImpl implements WeatherObservationRepository {

    private final WeatherObservationJpaRepository jpa;

    /**
     * <p>OpenWeather actualiza sus mediciones cada pocos minutos, así que dos
     * consultas seguidas devuelven la misma. Guardarla dos veces inflaría la
     * serie con puntos repetidos y sesgaría cualquier correlación.
     */
    @Override
    public void save(WeatherObservation observation) {
        if (jpa.findByObservedAt(observation.getObservedAt()).isPresent()) {
            return;
        }
        WeatherObservationEntity entity = new WeatherObservationEntity();
        entity.setObservedAt(observation.getObservedAt());
        entity.setTempC(observation.getTempC());
        entity.setRhPct(observation.getRhPct());
        entity.setCondition(observation.getCondition());
        entity.setFetchedAt(OffsetDateTime.now(ZoneOffset.UTC));
        jpa.save(entity);
    }

    @Override
    public List<WeatherObservation> findInRange(OffsetDateTime from, OffsetDateTime to) {
        return jpa.findInRange(from, to).stream()
                .map(e -> new WeatherObservation(
                        e.getObservedAt(), e.getTempC(), e.getRhPct(), e.getCondition()))
                .toList();
    }
}
