package com.pe.cloudapi.insights.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.alerting.interfaces.acl.AlertingContextFacade;
import com.pe.cloudapi.insights.domain.ports.out.ThresholdProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code insights} y {@code alerting}.
 */
@Service
@RequiredArgsConstructor
public class ExternalAlertingService implements ThresholdProvider {

    private final AlertingContextFacade alerting;

    @Override
    public Optional<Float> noiseWarnLevel(UUID roomTypeId) {
        return alerting.thresholdsOf(roomTypeId).stream()
                .filter(threshold -> threshold.getMetric() == ThresholdMetric.LAEQ)
                .map(Threshold::getWarnValue)
                .findFirst();
    }
}
