package com.pe.cloudapi.iam.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Acuse de recibo público: no devuelve los datos enviados. */
public record DemoRequestReceiptResource(UUID id, OffsetDateTime createdAt) {
}
