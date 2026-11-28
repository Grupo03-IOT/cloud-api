package com.pe.cloudapi.iam.application.internal.ports.in;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de entrada: el nombre de varias cuentas de una sola vez.
 */
public interface FindUserNamesUseCase {

    /**
     * @return el nombre por id; las cuentas que ya no existen no aparecen
     */
    Map<UUID, String> execute(Collection<UUID> userIds);
}
