package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;
import org.springframework.http.HttpStatus;

/**
 * Traduce la clase de error del dominio a un código de estado HTTP.
 *
 * <p>Vive en la capa de interfaces porque es la única que sabe que el
 * transporte es HTTP. Si mañana el mismo dominio se expusiera por otro
 * protocolo, solo cambiaría esta tabla.
 */
public final class ErrorKindHttpMapper {

    private ErrorKindHttpMapper() {
    }

    public static HttpStatus toStatus(ErrorKind kind) {
        return switch (kind) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}
