package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.commands.RegisterUserCommand;

public interface RegisterUserUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_EMAIL_ALREADY_USED} si ya existe una cuenta con ese correo
     */
    User execute(RegisterUserCommand command);
}
