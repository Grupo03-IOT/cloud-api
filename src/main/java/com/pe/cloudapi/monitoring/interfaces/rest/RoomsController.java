package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.GetLatestReading;
import com.pe.cloudapi.monitoring.application.internal.ports.in.GetReadingsInRange;
import com.pe.cloudapi.monitoring.application.internal.ports.in.GetRoom;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRooms;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListUnclassifiedRooms;
import com.pe.cloudapi.monitoring.application.internal.results.RoomReadings;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.queries.GetLatestReadingQuery;
import com.pe.cloudapi.monitoring.domain.model.queries.GetReadingsInRangeQuery;
import com.pe.cloudapi.monitoring.domain.model.queries.GetRoomQuery;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.ReadingResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.RoomResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.ReadingResourceAssemblerFromDomain;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.RoomResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Consulta de salas y de su telemetría.
 *
 * <p>Es lo que consumen la aplicación web y la móvil.
 *
 * <p>El controlador solo traduce: recibe parámetros HTTP, invoca un caso de uso
 * y convierte el resultado a recursos. No decide nada ni junta piezas — para
 * eso están los casos de uso.
 */
@Tag(name = "Rooms", description = "Room directory and telemetry queries")
@RestController
@RequestMapping(value = "/api/v1/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RoomsController {

    private final ListRooms listRoomsUseCase;
    private final ListUnclassifiedRooms listUnclassifiedRoomsUseCase;
    private final GetRoom getRoomUseCase;
    private final GetReadingsInRange getReadingsInRangeUseCase;
    private final GetLatestReading getLatestReadingUseCase;
    private final RoomResourceAssembler roomAssembler;
    private final ReadingResourceAssemblerFromDomain readingAssembler;

    /**
     * Directorio de salas con el resumen de su última lectura.
     */
    @GetMapping
    @Operation(
            summary = "List rooms with their latest reading",
            description = """
                    Returns every active room together with a summary of its most \
                    recent reading, including how many seconds old that reading is. \
                    Consumers decide what counts as too old for their use case.""")
    public List<RoomResource> listRooms() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return listRoomsUseCase.execute().stream()
                .map(snapshot -> roomAssembler.toResource(snapshot, now))
                .toList();
    }

    /**
     * Salas que aparecieron solas y nadie ha clasificado todavía.
     */
    @GetMapping("/unclassified")
    @Operation(summary = "List auto-registered rooms awaiting classification")
    public List<RoomResource> listUnclassified() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return listUnclassifiedRoomsUseCase.execute().stream()
                .map(snapshot -> roomAssembler.toResource(snapshot, now))
                .toList();
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get a single room")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room found"),
            @ApiResponse(responseCode = "404", description = "Room does not exist")
    })
    public RoomResource getRoom(@PathVariable UUID roomId) {
        return roomAssembler.toResource(
                getRoomUseCase.execute(new GetRoomQuery(roomId)),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Serie temporal de una sala.
     *
     * <p>El rango es obligatorio. No se asume ninguna ventana por defecto:
     * decidir qué periodo mirar es del cliente, y devolverle en silencio uno
     * que no pidió es peor que rechazar la petición.
     */
    @GetMapping("/{roomId}/readings")
    @Operation(
            summary = "Get per-minute readings for a room",
            description = "Both ends of the range are required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Readings returned"),
            @ApiResponse(responseCode = "400",
                    description = "Missing, inverted or invalid range"),
            @ApiResponse(responseCode = "404", description = "Room does not exist")
    })
    public List<ReadingResource> getReadings(
            @PathVariable UUID roomId,

            @Parameter(description = "Inicio del rango, UTC", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,

            @Parameter(description = "Fin del rango, UTC", required = true)
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {

        return toResources(getReadingsInRangeUseCase.execute(
                new GetReadingsInRangeQuery(roomId, from, to)));
    }

    @GetMapping("/{roomId}/readings/latest")
    @Operation(summary = "Get the most recent reading for a room")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reading returned"),
            @ApiResponse(responseCode = "404", description = "Room does not exist, "
                    + "or has not reported yet")
    })
    public ReadingResource getLatestReading(@PathVariable UUID roomId) {
        return toResources(getLatestReadingUseCase.execute(new GetLatestReadingQuery(roomId))).getFirst();
    }

    private List<ReadingResource> toResources(RoomReadings result) {
        return result.readings().stream()
                .map(reading -> readingAssembler.toResource(reading, result.room().getCode()))
                .toList();
    }
}
