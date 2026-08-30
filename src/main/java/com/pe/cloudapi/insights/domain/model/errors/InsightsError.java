package com.pe.cloudapi.insights.domain.model.errors;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

/**
 * Catálogo de errores del bounded context <em>Insights</em>.
 */
public enum InsightsError implements ErrorCatalog {

    RANGE_INVERTED(ErrorKind.VALIDATION,
            "The start of the range is after its end: %s > %s"),

    RANGE_TOO_SHORT(ErrorKind.UNPROCESSABLE,
            "A range of at least %s minutes is needed to compute anything meaningful");

    private static final String PREFIX = "INSIGHTS";

    private final ErrorKind kind;
    private final String messageTemplate;

    InsightsError(ErrorKind kind, String messageTemplate) {
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
