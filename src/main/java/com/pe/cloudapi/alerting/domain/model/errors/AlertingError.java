package com.pe.cloudapi.alerting.domain.model.errors;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

/**
 * Catálogo de errores del bounded context <em>Alerting</em>.
 *
 * <p>Todo error que este contexto puede devolver está aquí. Añadir uno nuevo en
 * cualquier otro sitio salta a la vista precisamente porque esta lista existe.
 */
public enum AlertingError implements ErrorCatalog {

    UNKNOWN_THRESHOLD_METRIC(ErrorKind.VALIDATION,
            "'%s' is not a metric a threshold can be set on"),

    THRESHOLD_NOT_FOUND(ErrorKind.NOT_FOUND,
            "No threshold is configured for room type %s and metric %s"),

    INVALID_THRESHOLD_RANGE(ErrorKind.VALIDATION,
            "The warning value must be below the critical one: %s >= %s");

    private static final String PREFIX = "ALERTING";

    private final ErrorKind kind;
    private final String messageTemplate;

    AlertingError(ErrorKind kind, String messageTemplate) {
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
