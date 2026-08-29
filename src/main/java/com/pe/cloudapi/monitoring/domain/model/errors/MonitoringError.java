package com.pe.cloudapi.monitoring.domain.model.errors;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;

/**
 * Catálogo de errores del bounded context <em>Monitoring</em>.
 *
 * <p>Todo error que este contexto puede devolver está aquí. Añadir uno nuevo en
 * cualquier otro sitio salta a la vista precisamente porque esta lista existe.
 *
 * <p>Los mensajes van en inglés, como el resto de lo que el producto expone.
 */
public enum MonitoringError implements ErrorCatalog {

    ROOM_NOT_FOUND(ErrorKind.NOT_FOUND,
            "Room %s does not exist"),

    ROOM_REQUIRED(ErrorKind.VALIDATION,
            "A room identifier is required"),

    ROOM_HAS_NO_READINGS(ErrorKind.NOT_FOUND,
            "Room %s has not reported any reading yet"),

    ROOM_NOT_CLASSIFIED(ErrorKind.UNPROCESSABLE,
            "Room %s has no room type assigned, so no thresholds apply to it"),

    RANGE_REQUIRED(ErrorKind.VALIDATION,
            "The range needs both a start and an end"),

    RANGE_INVERTED(ErrorKind.VALIDATION,
            "The start of the range is after its end: %s > %s"),

    EMPTY_BATCH(ErrorKind.VALIDATION,
            "The batch carries no readings"),

    NO_SITE_AVAILABLE(ErrorKind.CONFLICT,
            "No site is registered, so rooms cannot be attached to one");

    private static final String PREFIX = "MONITORING";

    private final ErrorKind kind;
    private final String messageTemplate;

    MonitoringError(ErrorKind kind, String messageTemplate) {
        this.kind = kind;
        this.messageTemplate = messageTemplate;
    }

    /**
     * Se deriva del nombre de la constante en vez de numerarse a mano: así no
     * hay códigos que colisionen ni huecos al borrar una entrada.
     */
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
