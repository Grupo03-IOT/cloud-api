package com.pe.cloudapi.iam.domain.model.valueobjects;

/**
 * Una credencial recién emitida y cuánto vale.
 *
 * @param value            lo que el cliente enviará como {@code Bearer}
 * @param expiresInSeconds segundos desde ahora
 */
public record IssuedToken(String value, long expiresInSeconds) {
}
