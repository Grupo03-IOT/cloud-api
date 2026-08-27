package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.ReadingResource;

import org.springframework.stereotype.Component;

/**
 * Traduce lecturas del dominio a su representación REST.
 *
 * <p>Se reutiliza {@link ReadingResource}, el mismo recurso con el que el Edge
 * las entrega. Que el contrato de entrada y el de salida coincidan no es
 * casualidad ni pereza: son la misma cosa, y tener dos formas distintas de
 * representar una lectura obligaría a mantener las dos sincronizadas.
 *
 * <p>El bloque {@code device} no se rellena aquí: pertenece al momento de la
 * ingesta y consultarlo por cada lectura devuelta sería una consulta extra por
 * fila para un dato que se repite en todas.
 */
@Component
public class ReadingResourceAssemblerFromDomain {

    /**
     * @param reading  lectura del dominio
     * @param roomCode código de la sala, que el agregado referencia por id
     */
    public ReadingResource toResource(RoomReading reading, String roomCode) {
        return new ReadingResource(
                roomCode,
                reading.getTs(),
                reading.getPeriodS(),
                null,
                new ReadingResource.Acoustic(
                        reading.getAcoustic().laeq(),
                        reading.getAcoustic().l10(),
                        reading.getAcoustic().l50(),
                        reading.getAcoustic().l90(),
                        reading.getAcoustic().lmax(),
                        reading.getAcoustic().lmin()),
                new ReadingResource.Climate(
                        reading.getClimate().tempC(),
                        reading.getClimate().rhPct()),
                new ReadingResource.Comfort(
                        reading.getComfort().pmv(),
                        reading.getComfort().ppd(),
                        reading.getComfort().verdict(),
                        null),
                new ReadingResource.Occupancy(
                        reading.getOccupancy().occupiedPct(),
                        reading.getOccupancy().transitions()),
                new ReadingResource.Quality(
                        reading.getQuality().batches(),
                        reading.getQuality().expected()));
    }
}
