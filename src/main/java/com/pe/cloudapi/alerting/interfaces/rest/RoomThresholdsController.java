package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.ResolveRoomThresholdsUseCase;
import com.pe.cloudapi.alerting.interfaces.rest.resources.RoomThresholdsResource;
import com.pe.cloudapi.alerting.interfaces.rest.transform.ThresholdResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Los umbrales que debe aplicar el Edge, resueltos sala por sala.
 *
 * <p>Existe porque la política se configura arriba y se aplica abajo: el
 * administrador fija umbrales por tipo de sala desde la Web App, y el Edge los
 * necesita por código de sala para poder evaluar sin preguntar a nadie.
 *
 * <p>Es un endpoint aparte y no un añadido a la respuesta de la ingesta, aunque
 * eso habría ahorrado una petición. Tres razones: cada contexto responde de lo
 * suyo, cada endpoint significa una cosa, y los fallos se separan — un lote
 * rechazado no debe dejar además al Edge sin configuración.
 */
@RestController
@RequestMapping(value = "/api/v1/room-thresholds", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Thresholds", description = "Alert thresholds")
public class RoomThresholdsController {

    private final ResolveRoomThresholdsUseCase resolveRoomThresholds;
    private final ThresholdResourceAssembler assembler;

    @GetMapping
    @Operation(summary = "List the thresholds in force for every room",
            description = """
                    Resolved per room, not per room type: the Edge knows its rooms \
                    by the code the device reports and does not model room types. \
                    A room with no thresholds is either unclassified or its type \
                    has none configured; the caller applies its own defaults.""")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Thresholds per room"))
    public List<RoomThresholdsResource> list() {
        return resolveRoomThresholds.execute().stream()
                .map(room -> new RoomThresholdsResource(
                        room.roomCode(),
                        room.thresholds().stream().map(assembler::toResource).toList()))
                .toList();
    }
}
