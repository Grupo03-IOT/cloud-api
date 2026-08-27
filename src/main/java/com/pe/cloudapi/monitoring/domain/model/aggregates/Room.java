package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.ClassifyRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Una sala instrumentada.
 *
 * <p>{@code code} es la clave natural configurada en el firmware y que viaja
 * en cada lote; el identificador es interno y el firmware nunca lo ve.
 *
 * <p>La sala se da de alta sola la primera vez que un dispositivo desconocido
 * reporta por ella, y queda sin clasificar hasta que un administrador le
 * asigna un {@link RoomType} desde la aplicación web.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Room {

    private final UUID id;
    private final UUID siteId;
    private final String code;
    @Setter private UUID roomTypeId;
    @Setter private String displayName;
    @Setter private String floor;
    @Setter private Integer capacity;
    @Setter private Float areaM2;
    @Setter private boolean active;

    public Room(CreateRoomCommand command) {
        this.id = null;
        this.siteId = command.siteId();
        this.code = command.code();
        this.displayName = command.displayName() == null ? command.code() : command.displayName();
        this.active = true;
    }

    public boolean isClassified() {
        return roomTypeId != null;
    }

    public void handle(ClassifyRoomCommand command) {
        this.roomTypeId = command.roomTypeId();
    }
}
