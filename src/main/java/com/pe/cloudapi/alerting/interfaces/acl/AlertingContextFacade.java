package com.pe.cloudapi.alerting.interfaces.acl;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;

import java.util.List;
import java.util.UUID;

/**
 * La puerta pública de {@code alerting} hacia los demás contextos.
 *
 * <p>Este contrato es toda la superficie: lo que no esté declarado aquí, no se expone.
 */
public interface AlertingContextFacade {

    /**
     * Los umbrales activos de un tipo de sala.
     *
     * @return vacío si el tipo no tiene ninguno activo
     */
    List<Threshold> thresholdsOf(UUID roomTypeId);
}
