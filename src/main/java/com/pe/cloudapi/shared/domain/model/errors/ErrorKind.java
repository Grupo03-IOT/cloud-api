package com.pe.cloudapi.shared.domain.model.errors;

/**
 * Naturaleza de un error, en términos de dominio.
 *
 * <p>Deliberadamente <strong>no</strong> son códigos HTTP: el dominio no sabe
 * que existe HTTP. La traducción a códigos de estado la hace la capa de
 * interfaces, que es la única que conoce el protocolo.
 */
public enum ErrorKind {

    VALIDATION,

    NOT_FOUND,

    CONFLICT,

    UNPROCESSABLE
}
