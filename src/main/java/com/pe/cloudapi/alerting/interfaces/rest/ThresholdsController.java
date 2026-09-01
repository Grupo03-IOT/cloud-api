package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.ConfigureThresholdUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.ListThresholdsUseCase;
import com.pe.cloudapi.alerting.domain.model.queries.ListThresholdsQuery;
import com.pe.cloudapi.alerting.interfaces.rest.resources.ConfigureThresholdResource;
import com.pe.cloudapi.alerting.interfaces.rest.resources.ThresholdResource;
import com.pe.cloudapi.alerting.interfaces.rest.transform.ThresholdResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Configuración de los umbrales que disparan alertas.
 *
 * <p>Es lo que administra la aplicación web: cambiar aquí un límite no exige
 * desplegar nada ni tocar el hardware.
 */
@Tag(name = "Thresholds", description = "Alert threshold configuration")
@RestController
@RequestMapping(value = "/api/v1/room-types/{roomTypeId}/thresholds",
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ThresholdsController {

    private final ConfigureThresholdUseCase configureThresholdUseCase;
    private final ListThresholdsUseCase listThresholdsUseCase;
    private final ThresholdResourceAssembler assembler;

    /**
     * Fija el umbral de una métrica.
     *
     * <p>Es {@code PUT} y no {@code POST} porque la operación es idempotente:
     * el par tipo de sala y métrica identifica al umbral, así que enviar dos
     * veces el mismo valor deja el sistema igual.
     */
    @PutMapping(value = "/{metric}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Set the threshold for a metric on a room type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Threshold stored"),
            @ApiResponse(responseCode = "400",
                    description = "Unknown metric, or warning value not below the critical one")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ThresholdResource configure(@PathVariable UUID roomTypeId,
                                       @PathVariable String metric,
                                       @Valid @RequestBody ConfigureThresholdResource resource) {
        return assembler.toResource(configureThresholdUseCase.execute(
                assembler.toCommand(roomTypeId, metric, resource)));
    }

    @GetMapping
    @Operation(summary = "List the enabled thresholds of a room type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    public List<ThresholdResource> list(@PathVariable UUID roomTypeId) {
        return listThresholdsUseCase.execute(new ListThresholdsQuery(roomTypeId))
                .stream().map(assembler::toResource).toList();
    }
}
