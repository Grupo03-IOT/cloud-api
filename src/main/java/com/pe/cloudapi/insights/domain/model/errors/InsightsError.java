package com.pe.cloudapi.insights.domain.model.errors;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

/**
 * Catálogo de errores del bounded context <em>Insights</em>.
 */
public enum InsightsError implements ErrorCatalog {

    PERIOD_INVALID(ErrorKind.VALIDATION,
            "'%s' is not a period: use a month (2026-10), an ISO week (2026-W42) or a day (2026-10-12)");

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
