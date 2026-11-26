package com.pe.cloudapi.shared.interfaces.rest;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * La persona que llama, leída del token.
 *
 * <p>El id sale siempre de la reclamación {@code sub}, nunca del cuerpo: si
 * viniera del cuerpo, cualquiera podría actuar en nombre de otro.
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    /**
     * @param jwt token de la petición; {@code null} si llamó una máquina
     * @return el id de usuario del token
     * @throws InsufficientAuthenticationException si no hay token de persona o
     *         su {@code sub} no es un id de usuario, que sale como 401
     */
    public static UUID idOf(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new InsufficientAuthenticationException("A user token is required");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new InsufficientAuthenticationException("The token subject is not a user id");
        }
    }
}
