package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.AcknowledgeAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.GetAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.ListAlertsUseCase;
import com.pe.cloudapi.alerting.domain.model.queries.GetAlertQuery;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AcknowledgeAlertResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.AlertResource;
import com.pe.cloudapi.alerting.interfaces.rest.transform.AlertResourceAssembler;
import com.pe.cloudapi.shared.interfaces.rest.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Alertas para el administrador: las activas, su reconocimiento y el historial.
 *
 * <p>El historial (US36) es este mismo listado con {@code status=CLOSED}; las
 * acciones correctivas (US37), con {@code has_action=true}.
 */
@Tag(name = "Alerts", description = "Sustained threshold breaches")
@RestController
@RequestMapping(value = "/api/v1/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AlertsController {

    private final ListAlertsUseCase listAlerts;
    private final GetAlertUseCase getAlert;
    private final AcknowledgeAlertUseCase acknowledgeAlert;
    private final AlertResourceAssembler assembler;

    @GetMapping
    @Operation(summary = "List alerts, newest first",
            description = "Every filter is optional. from and to apply to opened_at.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerts returned"),
            @ApiResponse(responseCode = "400", description = "Unknown status, or inverted range"),
            @ApiResponse(responseCode = "403", description = "Only administrators")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<AlertResource> list(
            @Parameter(description = "OPEN, ACKNOWLEDGED or CLOSED")
            @RequestParam(required = false) String status,

            @Parameter(description = "Room id")
            @RequestParam(name = "room_id", required = false) UUID roomId,

            @Parameter(description = "Opened at or after, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "Opened at or before, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,

            @Parameter(description = "true: only with a corrective action; false: only without")
            @RequestParam(name = "has_action", required = false) Boolean hasAction) {

        return listAlerts.execute(assembler.toQuery(status, roomId, from, to, hasAction))
                .stream().map(assembler::toResource).toList();
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get a single alert")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found"),
            @ApiResponse(responseCode = "403", description = "Only administrators"),
            @ApiResponse(responseCode = "404", description = "ALERTING_ALERT_NOT_FOUND")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public AlertResource get(@PathVariable UUID alertId) {
        return assembler.toResource(getAlert.execute(new GetAlertQuery(alertId)));
    }

    /**
     * Reconoce una alerta abierta. Quién la reconoce sale del token.
     */
    @PatchMapping(value = "/{alertId}/acknowledge", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Acknowledge an open alert with the corrective action taken")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert acknowledged"),
            @ApiResponse(responseCode = "400",
                    description = "ALERTING_CORRECTIVE_ACTION_REQUIRED: missing or not 3 to 500 characters"),
            @ApiResponse(responseCode = "403", description = "Only administrators"),
            @ApiResponse(responseCode = "404", description = "ALERTING_ALERT_NOT_FOUND"),
            @ApiResponse(responseCode = "409",
                    description = "ALERTING_ALERT_NOT_OPEN: already acknowledged or closed")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public AlertResource acknowledge(@PathVariable UUID alertId,
                                     @AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody AcknowledgeAlertResource resource) {
        return assembler.toResource(acknowledgeAlert.execute(
                assembler.toCommand(alertId, AuthenticatedUser.idOf(jwt), resource)));
    }
}
