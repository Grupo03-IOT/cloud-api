package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomTypeCommand;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.CreateRoomTypeResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.RoomTypeResource;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Traduce entre los recursos REST de tipos de sala y el dominio.
 */
@Component
public class RoomTypeResourceAssembler {

    /**
     * @param siteId   local al que pertenece, que viene en la ruta
     * @param resource cuerpo de la petición
     */
    public CreateRoomTypeCommand toCommand(UUID siteId, CreateRoomTypeResource resource) {
        return new CreateRoomTypeCommand(
                siteId, resource.code(), resource.displayName(), resource.description());
    }

    public RoomTypeResource toResource(RoomType roomType) {
        return new RoomTypeResource(
                roomType.getId(), roomType.getSiteId(), roomType.getCode(),
                roomType.getDisplayName(), roomType.getDescription());
    }
}
