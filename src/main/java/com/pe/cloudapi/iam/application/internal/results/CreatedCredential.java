package com.pe.cloudapi.iam.application.internal.results;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;

/**
 * La credencial recién creada y su clave en claro, que solo existe aquí.
 */
public record CreatedCredential(ApiCredential credential, String apiKey) {
}
