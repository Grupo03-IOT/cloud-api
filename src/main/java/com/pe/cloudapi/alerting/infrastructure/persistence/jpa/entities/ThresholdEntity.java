package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.AuditableModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Proyección de persistencia de un umbral. Tabla {@code threshold}.
 *
 * <p>{@code metric} se guarda como texto en minúsculas; el enum del dominio se
 * reconstruye en el mapper.
 */
@Entity
@Table(name = "threshold", schema = "alerting")
@Getter
@Setter
@NoArgsConstructor
public class ThresholdEntity extends AuditableModel {

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "metric", nullable = false, length = 24)
    private String metric;

    @Column(name = "warn_value", nullable = false)
    private Float warnValue;

    @Column(name = "critical_value")
    private Float criticalValue;

    @Column(name = "sustained_minutes", nullable = false)
    private Integer sustainedMinutes;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;
}
