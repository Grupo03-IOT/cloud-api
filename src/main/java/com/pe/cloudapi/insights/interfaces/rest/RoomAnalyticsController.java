package com.pe.cloudapi.insights.interfaces.rest;

import com.pe.cloudapi.insights.application.internal.ports.in.AnalyzeRoomUseCase;
import com.pe.cloudapi.insights.application.internal.ports.in.ProfileRoomHourlyUseCase;
import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.domain.model.queries.ProfileRoomHourlyQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.interfaces.rest.resources.HourlyProfileResource;
import com.pe.cloudapi.insights.interfaces.rest.resources.RoomAnalyticsResource;
import com.pe.cloudapi.insights.interfaces.rest.transform.RoomAnalyticsResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Análisis estadístico de una sala.
 *
 * <p>Responde a preguntas que necesitan historia larga, no el estado de ahora:
 * si una sala es ruidosa por la gente o por sí misma, cuánto tiempo pasa sobre
 * su umbral y a qué horas.
 */
@Tag(name = "Insights", description = "Statistical analysis over room telemetry")
@RestController
@RequestMapping(value = "/api/v1/insights/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RoomAnalyticsController {

    private static final String PERIOD_DESCRIPTION =
            "Un mes (2026-10), una semana ISO (2026-W42) o un día (2026-10-12), en la zona del local";

    private final AnalyzeRoomUseCase analyzeRoomUseCase;
    private final ProfileRoomHourlyUseCase profileRoomHourlyUseCase;
    private final RoomAnalyticsResourceAssembler assembler;

    @GetMapping("/{roomId}")
    @Operation(
            summary = "Analyse a room over a period",
            description = """
                    Findings first: only what is conclusive, possibly none. Then the three \
                    measures with their verdict, the noise episodes, the noise percentiles \
                    against the room type's threshold and the change from the previous period. \
                    Texts are not returned: every conclusion carries a stable code to translate.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis returned, even for a period without data"),
            @ApiResponse(responseCode = "400", description = "The period is missing or malformed"),
            @ApiResponse(responseCode = "404", description = "Room does not exist")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public RoomAnalyticsResource analyse(
            @PathVariable UUID roomId,
            @Parameter(description = PERIOD_DESCRIPTION, required = true, example = "2026-10")
            @RequestParam String period) {
        return assembler.toResource(
                analyzeRoomUseCase.execute(new AnalyzeRoomQuery(roomId, Period.parse(period))));
    }

    @GetMapping("/{roomId}/hourly-profile")
    @Operation(
            summary = "Profile a room hour by hour over a period",
            description = """
                    Twenty-four slots, one per hour of the day in the site's timezone. \
                    Noise is averaged by energy, not arithmetically.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "400", description = "The period is missing or malformed"),
            @ApiResponse(responseCode = "404", description = "Room does not exist")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public HourlyProfileResource hourlyProfile(
            @PathVariable UUID roomId,
            @Parameter(description = PERIOD_DESCRIPTION, required = true, example = "2026-10")
            @RequestParam String period) {
        return assembler.toResource(
                profileRoomHourlyUseCase.execute(new ProfileRoomHourlyQuery(roomId, Period.parse(period))));
    }
}
