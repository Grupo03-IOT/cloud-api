package com.pe.cloudapi.monitoring.domain.model.commands;

import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;

import java.util.Objects;
import java.util.UUID;

/**
 * Un miembro reporta una molestia en una sala.
 *
 * @param roomId   sala
 * @param userId   quién reporta; sale del token, nunca del cuerpo
 * @param category motivo
 * @param comment  texto libre opcional, hasta 500 caracteres; vacío equivale a ninguno
 */
public record FileDiscomfortReportCommand(UUID roomId, UUID userId, DiscomfortCategory category,
                                          String comment) {

    public static final int COMMENT_MAX = 500;

    public FileDiscomfortReportCommand {
        Objects.requireNonNull(roomId, "roomId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(category, "category");
        comment = comment == null || comment.isBlank() ? null : comment.strip();
        if (comment != null && comment.length() > COMMENT_MAX) {
            throw MonitoringError.DISCOMFORT_COMMENT_TOO_LONG.with();
        }
    }
}
