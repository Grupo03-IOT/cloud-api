package com.pe.cloudapi.iam.domain.model.valueobjects;

/**
 * Qué puede hacer una persona.
 */
public enum Role {

    /** Consulta el estado y la analítica de las salas. */
    MEMBER,

    /** Además: crea locales y tipos de sala, clasifica salas y fija umbrales. */
    ADMIN;

    public static Role fromCode(String code) {
        return valueOf(code.toUpperCase());
    }

    public String toCode() {
        return name();
    }
}
