package com.pe.cloudapi.alerting.infrastructure.persistence.jpa.entities;

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
 * Proyección de persistencia de una alerta. Tabla {@code alert}.
 *
 * <p>Hereda de {@code BaseModel} y no de {@code AuditableModel}: la abre una
 * máquina y su trazabilidad está en sus propias fechas ({@code opened_at},
 * {@code acknowledged_at}, {@code closed_at}).
 */
@Entity
@Table(name = "alert", schema = "alerting")
@Getter
@Setter
@NoArgsConstructor
public class AlertEntity extends BaseModel {

    @Column(name = "source_key", nullable = false, length = 120, unique = true)
    private String sourceKey;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "rule", nullable = false, length = 40)
    private String rule;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "message", nullable = false, length = 200)
    private String message;

    @Column(name = "value")
    private Double value;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "closed_by", length = 8)
    private String closedBy;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "corrective_action", length = 500)
    private String correctiveAction;
}
