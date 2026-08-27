package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Lote de lecturas que sube el Edge.
 *
 * <p>Manda varios minutos de golpe porque cuando se cae internet los encola y
 * los suelta todos juntos al volver la conexión.
 */
@Schema(description = "Lote de lecturas subido por el Edge")
public record ReadingBatchResource(

        @NotEmpty(message = "el lote no puede venir vacío")
        @Valid
        List<ReadingResource> readings

) {}
