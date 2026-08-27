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

    /**
     * Da de alta una sala. Nace activa y sin clasificar.
     *
     * @param command alta con el local, el código del firmware y el nombre
     *                visible
     */
    public Room(CreateRoomCommand command) {
        this.id = null;
        this.siteId = command.siteId();
        this.code = command.code();
        this.displayName = command.displayName() == null ? command.code() : command.displayName();
        this.active = true;
    }

    /**
     * Indica si un administrador ya asignó un tipo a la sala.
     *
     * <p>Mientras no lo esté, la sala no tiene umbrales aplicables, porque los
     * umbrales cuelgan del tipo y no de la sala.
     *
     * @return {@code true} si tiene tipo asignado
     */
    public boolean isClassified() {
        return roomTypeId != null;
    }

    /**
     * Asigna el tipo de sala. A partir de aquí le aplican los umbrales de ese
     * tipo.
     *
     * @param command tipo a asignar
     */
    public void handle(ClassifyRoomCommand command) {
        this.roomTypeId = command.roomTypeId();
    }
}
