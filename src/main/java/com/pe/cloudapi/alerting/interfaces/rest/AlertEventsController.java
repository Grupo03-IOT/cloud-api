package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.RecordAlertEventsUseCase;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AcceptedAlertEventsResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AlertEventBatchResource;
import com.pe.cloudapi.alerting.interfaces.rest.transform.AlertResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada de las alertas que abre y cierra el Edge.
 *
 * <p>El Edge evalúa los umbrales sin depender de internet y sube aquí lo que
 * detectó en cuanto puede. Es el único cliente de este endpoint.
 */
@Tag(name = "Alerts", description = "Sustained threshold breaches")
@RestController
@RequestMapping(value = "/api/v1/alerts/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AlertEventsController {

    private final RecordAlertEventsUseCase recordAlertEvents;
    private final AlertResourceAssembler assembler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Record alert events raised by the Edge",
            description = """
                    Idempotent by source_key: an event whose key is already known \
                    updates the alert (for example, adds closed_at) instead of \
                    duplicating it. A closing event moves the alert to CLOSED with \
                    closed_by = AUTO whether or not it was acknowledged. room_id is \
                    the room code, as in the readings. If any room is unknown the \
                    whole batch is rejected and nothing is stored.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events stored"),
            @ApiResponse(responseCode = "400", description = "Malformed or invalid batch"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
            @ApiResponse(responseCode = "403", description = "The key lacks the alerts:write scope"),
            @ApiResponse(responseCode = "404",
                    description = "MONITORING_ROOM_NOT_FOUND: a room code is unknown; do not retry")
    })
    @PreAuthorize("hasAuthority('SCOPE_alerts:write')")
    public AcceptedAlertEventsResource record(@Valid @RequestBody AlertEventBatchResource batch) {
        return new AcceptedAlertEventsResource(recordAlertEvents.execute(assembler.toCommands(batch)));
    }
}
