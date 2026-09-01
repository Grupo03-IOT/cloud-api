package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.application.internal.results.CreatedCredential;

import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;

import java.util.Set;

public interface CreateApiCredentialUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_CREDENTIAL_CODE_ALREADY_USED} si el código ya existe
     */
    CreatedCredential execute(String code, Set<Scope> scopes);
}
