package com.pe.cloudapi.iam.domain.ports.out;

/**
 * Cifrar y comparar contraseñas.
 */
public interface PasswordHasher {

    String hash(String plainPassword);

    /** Debe comparar en tiempo constante. */
    boolean matches(String plainPassword, String hash);
}
