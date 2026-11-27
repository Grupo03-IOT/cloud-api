package com.pe.cloudapi.iam.application.internal.ports.in;

import java.util.UUID;

/**
 * Puerto de entrada: si una cuenta puede seguir usando sus tokens.
 */
public interface IsUserActiveUseCase {

    /**
     * @return {@code false} si la cuenta está desactivada o ya no existe
     */
    boolean execute(UUID userId);
}
