package com.pe.cloudapi.insights.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Proyección de persistencia de una observación exterior.
 * Tabla {@code insights.weather_observation}.
 *
 * <p>Sin auditoría: la observación la genera un muestreador, no una persona.
 * {@code fetchedAt} frente a {@code observedAt} distingue cuándo la pedimos de
 * cuándo la midió el proveedor.
 */
@Entity
@Table(name = "weather_observation", schema = "insights")
@Getter
@Setter
@NoArgsConstructor
public class WeatherObservationEntity extends BaseModel {

    @Column(name = "observed_at", nullable = false)
    private OffsetDateTime observedAt;

    @Column(name = "temp_c")
    private Float tempC;

    @Column(name = "rh_pct")
    private Float rhPct;

    @Column(name = "condition", length = 64)
    private String condition;

    @Column(name = "fetched_at", nullable = false)
    private OffsetDateTime fetchedAt;
}
