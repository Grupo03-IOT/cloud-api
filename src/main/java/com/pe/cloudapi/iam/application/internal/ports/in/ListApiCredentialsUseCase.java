package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;

import java.util.List;

/**
 * Puerto de entrada: las credenciales de máquina, revocadas incluidas.
 */
public interface ListApiCredentialsUseCase {

    List<ApiCredential> execute();
}
