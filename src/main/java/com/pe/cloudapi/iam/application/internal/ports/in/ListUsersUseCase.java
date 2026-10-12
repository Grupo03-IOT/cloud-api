package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.User;

import java.util.List;

/**
 * Puerto de entrada: las cuentas, ordenadas por correo.
 */
public interface ListUsersUseCase {

    List<User> execute();
}
