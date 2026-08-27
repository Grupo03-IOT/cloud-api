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
 * Proyección de persistencia de una sala. Tabla {@code room}.
 */
@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class RoomEntity extends AuditableModel {

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "room_type_id")
    private UUID roomTypeId;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "floor", length = 32)
    private String floor;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "area_m2")
    private Float areaM2;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
