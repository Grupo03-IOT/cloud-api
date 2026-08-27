package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección de persistencia de una lectura por minuto. Tabla
 * {@code room_reading}.
 *
 * <p>Es la única entidad que hereda de {@code BaseModel} y no de
 * {@code AuditableModel}: la telemetría es inmutable y generada por máquina,
 * así que no lleva columnas de auditoría. La trazabilidad que sí importa aquí
 * es {@code receivedAt}.
 */
@Entity
@Table(name = "room_reading")
@Getter
@Setter
@NoArgsConstructor
public class RoomReadingEntity extends BaseModel {

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "ts", nullable = false)
    private OffsetDateTime ts;

    @Column(name = "period_s", nullable = false)
    private Integer periodS;

    @Column(name = "laeq") private Float laeq;
    @Column(name = "l10")  private Float l10;
    @Column(name = "l50")  private Float l50;
    @Column(name = "l90")  private Float l90;
    @Column(name = "lmax") private Float lmax;
    @Column(name = "lmin") private Float lmin;

    @Column(name = "temp_c") private Float tempC;
    @Column(name = "rh_pct") private Float rhPct;

    @Column(name = "pmv") private Float pmv;
    @Column(name = "ppd") private Float ppd;

    @Column(name = "thermal_verdict", length = 24)
    private String thermalVerdict;

    @Column(name = "occupied_pct", nullable = false)
    private Float occupiedPct;

    @Column(name = "transitions", nullable = false)
    private Integer transitions;

    @Column(name = "batches")  private Integer batches;
    @Column(name = "expected") private Integer expected;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;
}
