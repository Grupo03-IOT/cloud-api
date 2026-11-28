package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.FileDiscomfortReportUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDiscomfortReportsUseCase;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DiscomfortReportResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DiscomfortSummaryResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.FileDiscomfortReportResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.DiscomfortReportResourceAssembler;
import com.pe.cloudapi.shared.interfaces.rest.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Reportes de incomodidad: el miembro dice qué le molesta y el administrador
 * lo ve junto a lo que marcaba el sensor (US27, US28).
 */
@Tag(name = "Discomfort reports", description = "Member feedback on noise and temperature")
@RestController
@RequestMapping(value = "/api/v1/discomfort-reports", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DiscomfortReportsController {

    private final FileDiscomfortReportUseCase fileReport;
    private final ListDiscomfortReportsUseCase listReports;
    private final DiscomfortReportResourceAssembler assembler;

    /**
     * El usuario sale del token; un {@code user_id} en el cuerpo se ignora.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "File a discomfort report for a room",
            description = """
                    Stores the latest reading of the room together with the report, \
                    or null if none is newer than 10 minutes. A user can file the same \
                    category in the same room once every 10 minutes.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Report stored"),
            @ApiResponse(responseCode = "400", description = "Missing room, unknown category or comment too long"),
            @ApiResponse(responseCode = "401", description = "No user token"),
            @ApiResponse(responseCode = "404", description = "MONITORING_ROOM_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "MONITORING_DISCOMFORT_TOO_FREQUENT")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public ResponseEntity<DiscomfortReportResource> file(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FileDiscomfortReportResource resource) {
        var created = fileReport.execute(assembler.toCommand(AuthenticatedUser.idOf(jwt), resource));
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResource(created));
    }

    @GetMapping
    @Operation(summary = "List discomfort reports, newest first",
            description = "Every filter is optional. from and to apply to created_at.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reports returned"),
            @ApiResponse(responseCode = "400", description = "Unknown category or inverted range"),
            @ApiResponse(responseCode = "403", description = "Only administrators")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiscomfortReportResource> list(
            @Parameter(description = "Room id")
            @RequestParam(name = "room_id", required = false) UUID roomId,

            @Parameter(description = "NOISE, WARM or COLD")
            @RequestParam(required = false) String category,

            @Parameter(description = "Created at or after, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "Created at or before, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,

            @Parameter(description = "true: only with a discrepancy; false: only without")
            @RequestParam(required = false) Boolean discrepancy) {

        return listReports.execute(assembler.toQuery(roomId, category, from, to, discrepancy))
                .stream().map(assembler::toResource).toList();
    }

    @GetMapping("/mine")
    @Operation(summary = "List the reports filed by the authenticated user, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reports returned"),
            @ApiResponse(responseCode = "401", description = "No user token")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<DiscomfortReportResource> mine(@AuthenticationPrincipal Jwt jwt) {
        return listReports.ofUser(AuthenticatedUser.idOf(jwt))
                .stream().map(assembler::toResource).toList();
    }

    @GetMapping("/summary")
    @Operation(summary = "Count reports per room and category",
            description = "Only rooms with at least one report in the range; most reported first. "
                    + "discrepancies counts the reports the reading did not back.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "400", description = "Inverted range"),
            @ApiResponse(responseCode = "403", description = "Only administrators")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<DiscomfortSummaryResource> summary(
            @Parameter(description = "Created at or after, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "Created at or before, UTC")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        ListDiscomfortReportsQuery range = ListDiscomfortReportsQuery.between(from, to);
        return listReports.summarize(range.from(), range.to())
                .stream().map(assembler::toResource).toList();
    }
}
