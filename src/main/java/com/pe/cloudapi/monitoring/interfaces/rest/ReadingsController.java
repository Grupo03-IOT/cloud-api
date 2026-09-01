package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.IngestReadingsUseCase;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.IngestResultResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.ReadingBatchResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.ReadingResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada de la telemetría que sube el Edge.
 *
 * <p>Es el único cliente de este endpoint: los dispositivos nunca hablan
 * directamente con el cloud.
 */
@Tag(name = "Readings", description = "Telemetry ingestion from the Edge layer")
@RestController
@RequestMapping(value = "/api/v1/readings", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReadingsController {

    private final IngestReadingsUseCase ingestReadingsUseCase;
    private final ReadingResourceAssembler assembler;

    /**
     * Recibe un lote de agregados por minuto.
     *
     * <p>Responde {@code 202 Accepted}: el Edge solo necesita saber que el lote
     * quedó a salvo para poder descartarlo de su cola. Cualquier respuesta que
     * no sea 2xx hace que reintente el lote entero.
     *
     * @param batch lecturas subidas por el Edge
     * @return cuántas se guardaron y cuántas eran repetidas
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Ingest a batch of per-minute readings",
            description = """
                    Accepts aggregates produced by the Edge layer. Delivery is \
                    at-least-once, so the same minute may arrive more than once; \
                    duplicates are detected by (room, timestamp) and reported as \
                    updated rather than rejected. Unknown rooms and devices are \
                    registered automatically.""")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Batch accepted"),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid batch")
    })
    @PreAuthorize("hasAuthority('SCOPE_readings:write')")
    public ResponseEntity<IngestResultResource> ingest(
            @Valid @RequestBody ReadingBatchResource batch) {
        var result = ingestReadingsUseCase.execute(assembler.toCommand(batch.readings()));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(assembler.toResource(result));
    }
}
