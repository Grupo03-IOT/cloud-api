package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.AuditableModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Proyección de persistencia de un tipo de sala. Tabla {@code room_type}.
 */
@Entity
@Table(name = "room_type", schema = "monitoring")
@Getter
@Setter
@NoArgsConstructor
public class RoomTypeEntity extends AuditableModel {

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "description", length = 256)
    private String description;
}
