package com.pe.cloudapi.insights.interfaces.rest;

import com.pe.cloudapi.insights.application.internal.ports.in.AnalyzeRoomUseCase;
import com.pe.cloudapi.insights.domain.model.queries.AnalyzeRoomQuery;
import com.pe.cloudapi.insights.interfaces.rest.resources.RoomAnalyticsResource;
import com.pe.cloudapi.insights.interfaces.rest.transform.RoomAnalyticsResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Análisis estadístico de las salas.
 *
 * <p>Responde a preguntas que necesitan historia larga, no el estado de ahora:
 * si una sala es ruidosa por la gente o por sí misma, y si su climatización da
 * abasto.
 */
@Tag(name = "Insights", description = "Statistical analysis over room telemetry")
@RestController
@RequestMapping(value = "/api/v1/insights/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RoomAnalyticsController {

    private final AnalyzeRoomUseCase analyzeRoomUseCase;
    private final RoomAnalyticsResourceAssembler assembler;

    @GetMapping("/{roomId}")
    @Operation(
            summary = "Analyse a room over a period",
            description = """
                    Correlates noise against occupancy, fits the thermal drift and \
                    flags noise peaks that are anomalous for this particular room. \
                    Every figure carries its sample size and whether it is backed by \
                    enough data to be used.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis returned"),
            @ApiResponse(responseCode = "400", description = "Missing or inverted range"),
            @ApiResponse(responseCode = "422", description = "Not enough data in the range")
    })
    public RoomAnalyticsResource analyse(
            @PathVariable UUID roomId,

            @Parameter(description = "Inicio del rango, UTC", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "Fin del rango, UTC", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        return assembler.toResource(
                analyzeRoomUseCase.execute(new AnalyzeRoomQuery(roomId, from, to)));
    }
}
