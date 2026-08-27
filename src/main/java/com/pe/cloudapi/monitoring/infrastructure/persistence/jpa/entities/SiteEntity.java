package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.AuditableModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Proyección de persistencia de un local. Tabla {@code site}.
 */
@Entity
@Table(name = "site")
@Getter
@Setter
@NoArgsConstructor
public class SiteEntity extends AuditableModel {

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "address", length = 256)
    private String address;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;
}
