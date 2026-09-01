package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

import java.util.Map;

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
            "An unexpected error occurred"),

    /**
     * Un valor del cuerpo con un formato que su tipo no admite: una fecha que
     * no es fecha, un número donde se esperaba texto.
     */
    MALFORMED_FIELD(ErrorKind.VALIDATION,
            "The value has a format this field does not accept"),

    NOT_BLANK(ErrorKind.VALIDATION,
            "This field cannot be empty"),

    NOT_NULL(ErrorKind.VALIDATION,
            "This field is required"),

    NOT_EMPTY(ErrorKind.VALIDATION,
            "This must contain at least one element"),

    INVALID_SIZE(ErrorKind.VALIDATION,
            "This field's length is out of range"),

    NOT_POSITIVE(ErrorKind.VALIDATION,
            "This must be greater than zero"),

    /** La restricción que falló todavía no tiene entrada propia. */
    CONSTRAINT_VIOLATED(ErrorKind.VALIDATION,
            "This field does not satisfy its constraint");

    private static final String PREFIX = "API";

    /**
     * Qué entrada corresponde a cada restricción de Jakarta.
     *
     * <p>Se declara aquí en vez de derivarse del nombre de la anotación para
     * que <strong>ningún código salga al cliente sin estar escrito en un
     * catálogo</strong>. Una anotación nueva sin su línea aquí no rompe nada:
     * sale {@link #CONSTRAINT_VIOLATED}, que se ve en la respuesta y avisa de
     * que falta añadirla.
     */
    private static final Map<String, ApiError> BY_CONSTRAINT = Map.of(
            "NotBlank", NOT_BLANK,
            "NotNull", NOT_NULL,
            "NotEmpty", NOT_EMPTY,
            "Size", INVALID_SIZE,
            "Positive", NOT_POSITIVE);

    /**
     * @param constraint nombre simple de la anotación que falló, tal como lo
     *                   entrega Spring: {@code NotBlank}, {@code Size}
     */
    public static ApiError forConstraint(String constraint) {
        return BY_CONSTRAINT.getOrDefault(constraint, CONSTRAINT_VIOLATED);
    }

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
