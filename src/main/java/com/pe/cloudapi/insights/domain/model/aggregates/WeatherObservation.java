package com.pe.cloudapi.insights.domain.model.aggregates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Una medición del tiempo en el exterior.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class WeatherObservation {

    private final OffsetDateTime observedAt;
    private final Float tempC;
    private final Float rhPct;
    private final String condition;
}
