package com.pe.cloudapi.monitoring.domain.model.commands;

import java.util.UUID;

/**
 * Da de alta un tipo de sala dentro de un local.
 */
public record CreateRoomTypeCommand(
        UUID siteId,
        String code,
        String displayName,
        String description
) {}
