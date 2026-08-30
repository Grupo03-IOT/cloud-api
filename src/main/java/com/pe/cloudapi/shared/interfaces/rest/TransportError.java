package com.pe.cloudapi.shared.interfaces.rest;

import org.springframework.http.HttpStatus;

/**
 * Errores de la conversación HTTP, anteriores a cualquier controlador.
 * <p>Para el cliente son indistinguibles del resto: mismo prefijo {@code API_},
 * misma forma de respuesta.
 */
public enum TransportError {

    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND,
            "No endpoint matches %s %s"),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,
            "The %s method is not supported by this endpoint"),

    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "The content type '%s' is not supported"),

    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE,
            "This endpoint cannot produce any of the requested media types");

    private static final String PREFIX = "API";

    private final HttpStatus status;
    private final String messageTemplate;

    TransportError(HttpStatus status, String messageTemplate) {
        this.status = status;
        this.messageTemplate = messageTemplate;
    }

    /** Mismo prefijo que {@link ApiError}: para el cliente son el mismo catálogo. */
    public String code() {
        return PREFIX + "_" + name();
    }

    public HttpStatus status() {
        return status;
    }

    /**
     * @param args los huecos {@code %s} de la plantilla, en orden
     */
    public String message(Object... args) {
        return args.length == 0 ? messageTemplate : String.format(messageTemplate, args);
    }
}
