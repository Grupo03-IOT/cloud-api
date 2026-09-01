package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateRoomTypeUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateSiteUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomTypesUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSitesUseCase;
import com.pe.cloudapi.monitoring.domain.model.queries.ListRoomTypesQuery;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.CreateRoomTypeResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.CreateSiteResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.RoomTypeResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.SiteResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.RoomTypeResourceAssembler;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.SiteResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Administración de locales y de sus tipos de sala.
 *
 * <p>Es la puerta de entrada del sistema: <strong>sin un local no se puede
 * ingerir telemetría</strong>, porque las salas cuelgan de él.
 */
@Tag(name = "Sites", description = "Site and room type administration")
@RestController
@RequestMapping(value = "/api/v1/sites", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SitesController {

    private final CreateSiteUseCase createSiteUseCase;
    private final ListSitesUseCase listSitesUseCase;
    private final CreateRoomTypeUseCase createRoomTypeUseCase;
    private final ListRoomTypesUseCase listRoomTypesUseCase;
    private final SiteResourceAssembler siteAssembler;
    private final RoomTypeResourceAssembler roomTypeAssembler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a site")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Site created"),
            @ApiResponse(responseCode = "409", description = "The code is already used")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public SiteResource createSite(@Valid @RequestBody CreateSiteResource resource) {
        return siteAssembler.toResource(
                createSiteUseCase.execute(siteAssembler.toCommand(resource)));
    }

    @GetMapping
    @Operation(summary = "List sites")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<SiteResource> listSites() {
        return listSitesUseCase.execute().stream().map(siteAssembler::toResource).toList();
    }

    @PostMapping(value = "/{siteId}/room-types", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a room type",
            description = """
                    Room types are what give thresholds their meaning: the noise \
                    level that is unacceptable in a call booth is normal in a \
                    common area.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Room type created"),
            @ApiResponse(responseCode = "404", description = "The site does not exist"),
            @ApiResponse(responseCode = "409", description = "The code is already used")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public RoomTypeResource createRoomType(@PathVariable UUID siteId,
                                           @Valid @RequestBody CreateRoomTypeResource resource) {
        return roomTypeAssembler.toResource(
                createRoomTypeUseCase.execute(roomTypeAssembler.toCommand(siteId, resource)));
    }

    @GetMapping("/{siteId}/room-types")
    @Operation(summary = "List the room types of a site")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room types returned"),
            @ApiResponse(responseCode = "404", description = "The site does not exist")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<RoomTypeResource> listRoomTypes(@PathVariable UUID siteId) {
        return listRoomTypesUseCase.execute(new ListRoomTypesQuery(siteId))
                .stream().map(roomTypeAssembler::toResource).toList();
    }
}
