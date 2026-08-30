package com.pe.cloudapi.insights.domain.ports.out;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Histórico de observaciones del exterior.
 *
 * <p>Existe porque el proveedor solo sirve el momento actual: para correlacionar
 * un periodo hay que haberlo ido guardando. Sin este histórico, la correlación
 * interior ↔ exterior no se podría calcular hacia atrás.
 */
public interface WeatherObservationRepository {

    /**
     * Guarda la observación si no había ya una de ese instante.
     *
     * @param observation medición del proveedor
     */
    void save(WeatherObservation observation);

    /**
     * @return las observaciones del rango, de más antigua a más reciente
     */
    List<WeatherObservation> findInRange(OffsetDateTime from, OffsetDateTime to);
}
