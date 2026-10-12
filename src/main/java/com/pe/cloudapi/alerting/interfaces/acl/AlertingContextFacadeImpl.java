package com.pe.cloudapi.alerting.interfaces.acl;

import com.pe.cloudapi.alerting.application.internal.ports.in.ListThresholdsUseCase;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.queries.ListThresholdsQuery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Delega en los casos de uso de {@code alerting}, nunca en sus repositorios.
 */
@Service
@RequiredArgsConstructor
public class AlertingContextFacadeImpl implements AlertingContextFacade {

    private final ListThresholdsUseCase listThresholds;

    @Override
    public List<Threshold> thresholdsOf(UUID roomTypeId) {
        return listThresholds.execute(new ListThresholdsQuery(roomTypeId));
    }
}
