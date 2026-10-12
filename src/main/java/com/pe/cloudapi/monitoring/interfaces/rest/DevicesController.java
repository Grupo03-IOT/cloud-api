package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDevicesUseCase;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDevicesQuery;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DeviceResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.DeviceResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Los módulos ESP32 que reportan por las salas.
 */
@Tag(name = "Devices", description = "ESP32 modules and their health")
@RestController
@RequestMapping(value = "/api/v1/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DevicesController {

    private final ListDevicesUseCase listDevicesUseCase;
    private final DeviceResourceAssembler assembler;

    @GetMapping
    @Operation(
            summary = "List devices and whether they are silent",
            description = """
                    Silent means no batch reached the Edge for more than five minutes, \
                    or none ever did. It is worked out at query time from the last batch.""")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Devices returned"))
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeviceResource> list(
            @Parameter(description = "Solo los de esta sala")
            @RequestParam(required = false) UUID roomId) {
        return listDevicesUseCase.execute(new ListDevicesQuery(roomId)).stream()
                .map(assembler::toResource)
                .toList();
    }
}
