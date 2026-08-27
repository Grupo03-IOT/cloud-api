package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomTypeCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Clasifica las salas por la actividad que albergan, que es lo que da sentido
 * a los umbrales: el nivel de ruido inaceptable en una cabina de llamadas es
 * perfectamente normal en una zona común.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class RoomType {

    private final UUID id;
    private final UUID siteId;
    private final String code;
    @Setter private String displayName;
    @Setter private String description;

    /**
     * Da de alta un tipo de sala dentro de un local.
     *
     * @param command alta con el local, el código y el nombre visible
     */
    public RoomType(CreateRoomTypeCommand command) {
        this.id = null;
        this.siteId = command.siteId();
        this.code = command.code();
        this.displayName = command.displayName();
        this.description = command.description();
    }
}
