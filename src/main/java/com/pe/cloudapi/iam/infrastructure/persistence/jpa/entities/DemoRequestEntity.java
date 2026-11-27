package com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Proyección de persistencia de una solicitud de demostración. Tabla {@code demo_request}. */
@Entity
@Table(name = "demo_request", schema = "iam")
@Getter
@Setter
@NoArgsConstructor
public class DemoRequestEntity extends BaseModel {

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "work_email", nullable = false, length = 150)
    private String workEmail;

    @Column(name = "company", nullable = false, length = 150)
    private String company;

    @Column(name = "room_count", nullable = false)
    private int roomCount;

    @Column(name = "consented_at", nullable = false, updatable = false)
    private OffsetDateTime consentedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
