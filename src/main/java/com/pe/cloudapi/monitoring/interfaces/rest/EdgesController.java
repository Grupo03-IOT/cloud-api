package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListGatewaysUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.RecordHeartbeatUseCase;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.GatewayResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.HeartbeatResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.GatewayResourceAssembler;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Si cada Edge sigue vivo.
 *
 * <p>El cloud no puede llamar al Edge, que vive tras el router del local: es el
 * Edge quien avisa cada minuto, y su silencio es la señal.
 */
@Tag(name = "Edges", description = "Edge liveness")
@RestController
@RequestMapping(value = "/api/v1/edges", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EdgesController {

    private final RecordHeartbeatUseCase recordHeartbeatUseCase;
    private final ListGatewaysUseCase listGatewaysUseCase;
    private final GatewayResourceAssembler assembler;

    @PostMapping(value = "/heartbeat", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Record an Edge heartbeat",
            description = """
                    Sent every minute even with nothing to upload, so that silence means \
                    something. The first heartbeat registers the Edge. The time recorded \
                    is when the cloud received it, not what the Edge says.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Heartbeat recorded"),
            @ApiResponse(responseCode = "400", description = "Malformed heartbeat"),
            @ApiResponse(responseCode = "404", description = "No site has that code")
    })
    @PreAuthorize("hasAuthority('SCOPE_edges:heartbeat')")
    public void heartbeat(@Valid @RequestBody HeartbeatResource resource) {
        recordHeartbeatUseCase.execute(resource.code(), resource.siteCode(), resource.version(),
                resource.pendingUpload(), resource.rooms());
    }

    @GetMapping
    @Operation(
            summary = "List Edges and whether they are alive",
            description = """
                    online: last heartbeat within 90 seconds. stale: within three minutes. \
                    offline: older, or none. Worked out at query time.""")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Edges returned"))
    @PreAuthorize("hasRole('ADMIN')")
    public List<GatewayResource> list() {
        return listGatewaysUseCase.execute().stream()
                .map(assembler::toResource)
                .toList();
    }
}
