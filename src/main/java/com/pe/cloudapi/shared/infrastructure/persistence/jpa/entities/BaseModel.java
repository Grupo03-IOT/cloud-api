package com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Identidad común a toda entidad persistida.
 *
 * <p>El identificador es un UUID versión 7 generado por Hibernate. La v7 lleva
 * los 48 bits más significativos como marca de tiempo Unix en milisegundos, de
 * modo que ordenar por {@code id} equivale a ordenar por fecha de creación y
 * las inserciones caen al final del índice en vez de dispersarlo, que es lo
 * que hace un UUID v4 aleatorio.
 */
@Getter
@MappedSuperclass
public abstract class BaseModel {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
}
