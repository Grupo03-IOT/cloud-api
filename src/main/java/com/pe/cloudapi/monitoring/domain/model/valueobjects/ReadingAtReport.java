package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;

import java.time.OffsetDateTime;

/**
 * Copia de la lectura de la sala en el momento de un reporte de incomodidad.
 *
 * <p>Se copia y no se referencia: lo que importa es lo que marcaba el sensor
 * cuando el miembro se quejó, aunque después la sala cambie.
 *
 * @param ts          minuto de la lectura
 * @param laeq        nivel sonoro equivalente, dBA
 * @param tempC       temperatura, °C
 * @param rhPct       humedad relativa, %
 * @param occupiedPct porcentaje del minuto con presencia
 */
public record ReadingAtReport(OffsetDateTime ts, Float laeq, Float tempC, Float rhPct, Float occupiedPct) {

    public static ReadingAtReport of(RoomReading reading) {
        return new ReadingAtReport(
                reading.getTs(),
                reading.getAcoustic() == null ? null : reading.getAcoustic().laeq(),
                reading.getClimate() == null ? null : reading.getClimate().tempC(),
                reading.getClimate() == null ? null : reading.getClimate().rhPct(),
                reading.getOccupancy() == null ? null : reading.getOccupancy().occupiedPct());
    }
}
