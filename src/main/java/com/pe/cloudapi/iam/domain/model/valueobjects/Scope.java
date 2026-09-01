package com.pe.cloudapi.iam.domain.model.valueobjects;

import com.pe.cloudapi.iam.domain.model.errors.IamError;

/**
 * Qué puede hacer una máquina.
 */
public enum Scope {

    READINGS_WRITE("readings:write"),

    THRESHOLDS_READ("thresholds:read");

    private final String code;

    Scope(String code) {
        this.code = code;
    }

    public String toCode() {
        return code;
    }

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_UNKNOWN_SCOPE} si no es ninguno de los declarados
     */
    public static Scope fromCode(String code) {
        for (Scope scope : values()) {
            if (scope.code.equalsIgnoreCase(code)) {
                return scope;
            }
        }
        throw IamError.UNKNOWN_SCOPE.with(code);
    }
}
