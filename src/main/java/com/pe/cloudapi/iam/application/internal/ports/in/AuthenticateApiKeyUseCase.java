package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;

import java.util.Optional;

/**
 * Resolver quién es el dueño de una clave.
 */
public interface AuthenticateApiKeyUseCase {

    /** @return vacío si la clave no existe o la credencial está revocada */
    Optional<ApiCredential> execute(String apiKey);
}
