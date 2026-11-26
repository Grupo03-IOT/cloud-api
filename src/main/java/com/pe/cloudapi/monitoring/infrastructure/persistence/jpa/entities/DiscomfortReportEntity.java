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
 * Proyección de persistencia de un reporte de incomodidad. Tabla
 * {@code discomfort_report}.
 *
 * <p>La lectura del momento va aplanada en columnas propias: es una copia, no
 * una referencia a {@code room_reading}.
 */
@Entity
@Table(name = "discomfort_report", schema = "monitoring")
@Getter
@Setter
@NoArgsConstructor
public class DiscomfortReportEntity extends BaseModel {

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category", nullable = false, length = 8)
    private String category;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "reading_ts")   private OffsetDateTime readingTs;
    @Column(name = "laeq")         private Float laeq;
    @Column(name = "temp_c")       private Float tempC;
    @Column(name = "rh_pct")       private Float rhPct;
    @Column(name = "occupied_pct") private Float occupiedPct;
}
