package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;

public interface AuthenticateUserUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_INVALID_CREDENTIALS} si el correo no existe o la
     *         contraseña no coincide — el mismo error para los dos casos
     */
    IssuedToken execute(String email, String plainPassword);
}
