package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.User;

import java.util.UUID;

/** Activa o desactiva la cuenta de un miembro. Una cuenta desactivada no puede iniciar sesión. */
public interface SetUserActiveUseCase {

    User execute(UUID userId, boolean active);
}
