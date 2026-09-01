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

    ROOM_HAS_NO_READINGS(ErrorKind.NOT_FOUND,
            "Room %s has not reported any reading yet"),

    SITE_NOT_FOUND(ErrorKind.NOT_FOUND,
            "Site %s does not exist"),

    SITE_CODE_ALREADY_USED(ErrorKind.CONFLICT,
            "A site with code '%s' already exists"),

    ROOM_TYPE_NOT_FOUND(ErrorKind.NOT_FOUND,
            "Room type %s does not exist"),

    ROOM_TYPE_CODE_ALREADY_USED(ErrorKind.CONFLICT,
            "Site %s already has a room type with code '%s'"),

    ROOM_TYPE_FROM_ANOTHER_SITE(ErrorKind.UNPROCESSABLE,
            "Room type %s belongs to a different site than room %s"),

    RANGE_INVERTED(ErrorKind.VALIDATION,
            "The start of the range is after its end: %s > %s"),

    NO_SITE_AVAILABLE(ErrorKind.CONFLICT,
            "No site is registered, so rooms cannot be attached to one"),

    READING_BATCH_EMPTY(ErrorKind.VALIDATION,
            "A batch must carry at least one reading"),

    READING_PERIOD_REQUIRED(ErrorKind.VALIDATION,
            "The period is required: assuming it would skew the energy averages");

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
