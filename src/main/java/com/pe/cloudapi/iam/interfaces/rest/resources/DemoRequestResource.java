package com.pe.cloudapi.iam.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Una solicitud tal como la ve el administrador. */
public record DemoRequestResource(
        UUID id,
        String fullName,
        String workEmail,
        String company,
        int roomCount,
        OffsetDateTime consentedAt,
        OffsetDateTime createdAt
) {
}
