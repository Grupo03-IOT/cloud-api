package com.pe.cloudapi.insights.domain.model.valueobjects;

/**
 * Cuánto respaldo tiene una conclusión. Se decide con él, nunca leyendo el valor.
 */
public enum Verdict {

    CONCLUSIVE,

    /** Hay datos, pero no bastan para sostener un número. */
    NOT_ENOUGH_EVIDENCE,

    NO_DATA;

    public String toCode() {
        return name().toLowerCase();
    }
}
