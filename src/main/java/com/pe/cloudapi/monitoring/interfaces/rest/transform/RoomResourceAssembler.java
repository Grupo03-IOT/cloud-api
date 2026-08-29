package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.RoomResource;

import org.springframework.stereotype.Component;


/**
 * Traduce salas del dominio a su representación REST.
 */
@Component
public class RoomResourceAssembler {

    /**
     * @param snapshot sala junto a su última lectura
     */
    public RoomResource toResource(RoomSnapshot snapshot) {
        Room room = snapshot.room();
        return new RoomResource(
                room.getId(),
                room.getCode(),
                room.getDisplayName(),
                room.getFloor(),
                room.getCapacity(),
                room.getAreaM2(),
                room.isActive(),
                room.isClassified(),
                snapshot.latest().map(this::toLatest).orElse(null));
    }

    private RoomResource.Latest toLatest(RoomReading reading) {
        return new RoomResource.Latest(
                reading.getTs(),
                reading.getAcoustic().laeq(),
                reading.getClimate().tempC(),
                reading.getClimate().rhPct(),
                reading.getComfort().ppd(),
                reading.getComfort().verdict(),
                reading.getOccupancy().occupiedPct(),
                reading.isReliable());
    }
}
