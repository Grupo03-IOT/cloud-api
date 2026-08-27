package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.RecordRoomReadingCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.AcousticMetrics;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThermalComfort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Un minuto de una sala: la foto de cuánto ruido, cuánto calor y cuánta gente
 * hubo durante ese periodo.
 *
 * <p>Las cifras acústicas y de confort llegan ya calculadas desde el Edge. El
 * cloud solo calcula lo que necesita historia larga o varias salas a la vez:
 * correlaciones, regresiones, predicción y comparativas.
 *
 * <p>Ojo con las dos fechas: {@code ts} es el minuto que la lectura describe y
 * {@code receivedAt} es cuándo llegó al cloud. Divergen cada vez que el Edge
 * vacía una cola acumulada durante un corte de red.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class RoomReading {

    private final UUID id;
    private final UUID roomId;
    private final OffsetDateTime ts;
    private final int periodS;
    private final AcousticMetrics acoustic;
    private final Climate climate;
    private final ThermalComfort comfort;
    private final Occupancy occupancy;
    private final DataQuality quality;
    private final OffsetDateTime receivedAt;

    /**
     * Registra el agregado de un minuto tal como lo sube el Edge.
     *
     * <p>Los value objects que lleguen nulos se sustituyen por su versión
     * vacía, para que quien consuma la lectura no tenga que comprobar nulos en
     * cada nivel.
     *
     * <p>{@code receivedAt} se sella aquí, no lo trae el comando: es el
     * instante en que el cloud recibe el dato, y no debe poder falsearlo quien
     * lo envía.
     *
     * @param command agregado del minuto, ya calculado en el Edge
     */
    public RoomReading(RecordRoomReadingCommand command) {
        this.id = null;
        this.roomId = command.roomId();
        this.ts = command.ts();
        this.periodS = command.periodS();
        this.acoustic = command.acoustic() == null ? AcousticMetrics.empty() : command.acoustic();
        this.climate = command.climate() == null ? Climate.empty() : command.climate();
        this.comfort = command.comfort() == null ? ThermalComfort.empty() : command.comfort();
        this.occupancy = command.occupancy() == null ? Occupancy.vacant() : command.occupancy();
        this.quality = command.quality() == null ? DataQuality.unknown() : command.quality();
        this.receivedAt = OffsetDateTime.now();
    }

    /**
     * Indica si el minuto está respaldado por todos los lotes que debía
     * recibir.
     *
     * <p>Una lectura poco fiable no es incorrecta, pero se construyó con menos
     * datos de los previstos, así que conviene excluirla de las correlaciones
     * y las regresiones.
     *
     * @return {@code true} si llegaron todos los lotes esperados
     */
    public boolean isReliable() {
        return quality != null && quality.isComplete();
    }
}
