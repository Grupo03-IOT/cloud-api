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
 * Proyección de persistencia de un dispositivo. Tabla {@code device}.
 *
 * <p>La columna {@code status} existe en la tabla pero <strong>no se mapea</strong>:
 * la conectividad es un dato derivado de {@code last_seen} y se calcula al
 * consultarla. La columna queda inerte, rellenada por su valor por defecto, a la
 * espera de una migración que la elimine o la reconvierta en estado
 * administrativo (dado de baja, en mantenimiento), que sí es un hecho que
 * alguien decide y no se deduce del silencio.
 */
@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
public class DeviceEntity extends AuditableModel {

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "fw_version", length = 32)
    private String fwVersion;

    @Column(name = "last_seen")
    private OffsetDateTime lastSeen;

    @Column(name = "last_seq")
    private Long lastSeq;

    @Column(name = "lost_batches", nullable = false)
    private Long lostBatches;
}
