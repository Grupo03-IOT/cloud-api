package com.pe.cloudapi.iam.domain.model.commands;

import com.pe.cloudapi.iam.domain.model.valueobjects.Role;

import java.util.Objects;
import java.util.Set;

/**
 * Alta de una cuenta.
 *
 * @param plainPassword sin cifrar. Es lo único que el dominio ve en claro, y
 *                      solo el tiempo de pasársela al cifrador
 */
public record RegisterUserCommand(String email, String plainPassword,
                                  String displayName, Set<Role> roles) {

    public RegisterUserCommand {
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(plainPassword, "plainPassword");
        email = email.trim().toLowerCase();
        displayName = displayName == null || displayName.isBlank() ? email : displayName.trim();
        roles = roles == null || roles.isEmpty() ? Set.of(Role.MEMBER) : Set.copyOf(roles);
    }
}
