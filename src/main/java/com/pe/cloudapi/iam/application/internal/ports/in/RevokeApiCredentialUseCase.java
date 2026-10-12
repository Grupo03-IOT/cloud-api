package com.pe.cloudapi.iam.application.internal.ports.in;

import java.util.UUID;

/**
 * Puerto de entrada: revocar una credencial de máquina.
 */
public interface RevokeApiCredentialUseCase {

    /**
     * Surte efecto en la siguiente petición de la máquina. Revocar una ya
     * revocada no es un error.
     *
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_CREDENTIAL_NOT_FOUND} si no existe
     */
    void execute(UUID credentialId);
}
