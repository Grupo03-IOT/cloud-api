package com.pe.cloudapi.iam.domain.model.events;

import java.util.UUID;

/**
 * Una cuenta se activó o se desactivó.
 *
 * <p>Lo escucha la caché de estado de cuentas para que una desactivación
 * corte los tokens vivos de inmediato y no al vencer la caché.
 */
public record UserActiveChangedEvent(UUID userId, boolean active) {
}
