package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

/**
 * Catálogo de errores de transporte, comunes a toda la API.
 *
 * <p>No son errores de negocio: ocurren antes de que la petición llegue a
 * ningún bounded context. Un JSON ilegible o un identificador mal formado no
 * son problemas de dominio, son de la conversación HTTP.
 *
 * <p>Vive en la capa de interfaces por eso mismo, pero implementa el mismo
 * contrato que los catálogos de dominio para que el cliente reciba todos los
 * errores con la misma forma, vengan de donde vengan.
 */
public enum ApiError implements ErrorCatalog {

    MALFORMED_REQUEST(ErrorKind.VALIDATION,
            "The request body could not be read"),

    VALIDATION_FAILED(ErrorKind.VALIDATION,
            "The request does not satisfy the contract"),

    INVALID_PARAMETER(ErrorKind.VALIDATION,
            "Parameter '%s' is not %s"),

    MISSING_PARAMETER(ErrorKind.VALIDATION,
            "Required parameter '%s' is missing"),

    /**
     * Lo que no estaba previsto. Un catálogo enumera lo que se anticipa, así
     * que esta entrada es justamente la que cubre lo que no.
     */
    INTERNAL_ERROR(ErrorKind.INTERNAL,
            "An unexpected error occurred");

    private static final String PREFIX = "API";

    private final ErrorKind kind;
    private final String messageTemplate;

    ApiError(ErrorKind kind, String messageTemplate) {
        this.kind = kind;
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String code() {
        return PREFIX + "_" + name();
    }

    @Override
    public ErrorKind kind() {
        return kind;
    }

    @Override
    public String messageTemplate() {
        return messageTemplate;
    }
}
