package com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Auditoría común a las entidades que edita una persona.
 *
 * <p>Las rellena Spring Data JPA Auditing, por eso las columnas no tienen
 * valor por defecto en la base.
 *
 * <p>{@code createdBy} y {@code updatedBy} quedan nulos mientras no exista el
 * contexto de identidad; cuando exista, basta con cambiar el
 * {@code AuditorAware} de la configuración.
 *
 * <p>La telemetría NO hereda de aquí: es inmutable y generada por máquina.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableModel extends BaseModel {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Setter
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markAsDeleted() {
        this.deletedAt = OffsetDateTime.now();
    }
}
