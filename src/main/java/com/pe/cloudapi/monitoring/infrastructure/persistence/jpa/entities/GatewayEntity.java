package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Proyección de persistencia de un Edge. Tabla {@code gateway}.
 *
 * <p>Sin columna de estado: se deriva de {@code last_seen} al consultar.
 */
@Entity
@Table(name = "gateway", schema = "monitoring")
@Getter
@Setter
@NoArgsConstructor
public class GatewayEntity extends AuditableModel {

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "last_seen", nullable = false)
    private OffsetDateTime lastSeen;

    @Column(name = "pending_upload", nullable = false)
    private Integer pendingUpload;

    @Column(name = "rooms_reported", nullable = false)
    private Integer roomsReported;
}
