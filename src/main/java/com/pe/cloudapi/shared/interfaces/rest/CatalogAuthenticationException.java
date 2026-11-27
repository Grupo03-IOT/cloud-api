package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

/**
 * Un rechazo de autenticación que lleva su entrada de catálogo.
 *
 * <p>La cadena de seguridad solo entiende {@link AuthenticationException}; sin
 * esta envoltura, cualquier rechazo saldría con el código genérico
 * {@code UNAUTHORIZED} y el cliente no sabría, por ejemplo, que su cuenta fue
 * desactivada.
 */
public class CatalogAuthenticationException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ErrorCatalog error;

    public CatalogAuthenticationException(DomainException cause) {
        super(cause.getMessage(), cause);
        this.error = cause.getError();
    }

    public ErrorCatalog getError() {
        return error;
    }
}
