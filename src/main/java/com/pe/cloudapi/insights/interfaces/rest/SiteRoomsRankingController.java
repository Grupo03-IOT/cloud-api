package com.pe.cloudapi.insights.interfaces.rest;

import com.pe.cloudapi.insights.application.internal.ports.in.RankSiteRoomsUseCase;
import com.pe.cloudapi.insights.domain.model.queries.RankSiteRoomsQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.interfaces.rest.resources.SiteRoomsRankingResource;
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
 * Comparativa de las salas de un local.
 */
@Tag(name = "Insights", description = "Statistical analysis over room telemetry")
@RestController
@RequestMapping(value = "/api/v1/insights/sites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SiteRoomsRankingController {

    private final RankSiteRoomsUseCase rankSiteRoomsUseCase;
    private final RoomAnalyticsResourceAssembler assembler;

    @GetMapping("/{siteId}/rooms")
    @Operation(
            summary = "Rank the rooms of a site over a period",
            description = """
                    Worst first: the rooms that spent the most time above their threshold, \
                    then the ones without a threshold by their p90, and last the ones \
                    without data.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking returned"),
            @ApiResponse(responseCode = "400", description = "The period is missing or malformed"),
            @ApiResponse(responseCode = "404", description = "Site does not exist")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public SiteRoomsRankingResource rank(
            @PathVariable UUID siteId,
            @Parameter(description = "A month (2026-10), an ISO week (2026-W42) or a day (2026-10-12)",
                    required = true, example = "2026-10")
            @RequestParam String period) {
        Period parsed = Period.parse(period);
        return assembler.toResource(siteId, parsed,
                rankSiteRoomsUseCase.execute(new RankSiteRoomsQuery(siteId, parsed)));
    }
}
