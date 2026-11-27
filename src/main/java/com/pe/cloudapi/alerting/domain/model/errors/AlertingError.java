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

    INVALID_THRESHOLD_RANGE(ErrorKind.VALIDATION,
            "The warning value must be below the critical one: %s >= %s"),

    ALERT_NOT_FOUND(ErrorKind.NOT_FOUND,
            "Alert %s does not exist"),

    ALERT_NOT_OPEN(ErrorKind.CONFLICT,
            "Alert %s is %s; only an open alert can be acknowledged"),

    CORRECTIVE_ACTION_REQUIRED(ErrorKind.VALIDATION,
            "A corrective action of 3 to 500 characters is required"),

    ALERT_BATCH_EMPTY(ErrorKind.VALIDATION,
            "A batch must carry at least one alert event"),

    ALERT_CLOSED_BEFORE_OPENED(ErrorKind.VALIDATION,
            "Alert %s cannot close (%s) before it opened (%s)"),

    UNKNOWN_ALERT_SEVERITY(ErrorKind.VALIDATION,
            "'%s' is not a known severity: info, warning or critical"),

    UNKNOWN_ALERT_STATUS(ErrorKind.VALIDATION,
            "'%s' is not a known alert status: OPEN, ACKNOWLEDGED or CLOSED"),

    INVALID_ALERT_RULE(ErrorKind.VALIDATION,
            "The rule must be a lowercase identifier of up to 40 characters"),

    RANGE_INVERTED(ErrorKind.VALIDATION,
            "The start of the range is after its end: %s > %s");

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
