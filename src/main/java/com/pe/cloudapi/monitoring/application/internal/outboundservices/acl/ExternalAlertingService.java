package com.pe.cloudapi.monitoring.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.alerting.interfaces.acl.AlertingContextFacade;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ComfortThresholds;
import com.pe.cloudapi.monitoring.domain.ports.out.ComfortThresholdProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code monitoring} y {@code alerting}.
 *
 * <p>Un umbral solo guarda límites superiores (aviso y crítico). El inferior de
 * confort térmico no se configura por tipo de sala: sale de
 * {@code app.monitoring.comfort-min-temp-c} y solo se aplica a los tipos que
 * tienen un umbral de temperatura activo.
 */
@Service
public class ExternalAlertingService implements ComfortThresholdProvider {

    private final AlertingContextFacade alerting;
    private final float comfortMinTempC;

    public ExternalAlertingService(AlertingContextFacade alerting,
                                   @Value("${app.monitoring.comfort-min-temp-c:20}") float comfortMinTempC) {
        this.alerting = alerting;
        this.comfortMinTempC = comfortMinTempC;
    }

    @Override
    public ComfortThresholds comfortThresholdsOf(UUID roomTypeId) {
        List<Threshold> thresholds = alerting.thresholdsOf(roomTypeId);
        Float noiseWarn = warnValueOf(thresholds, ThresholdMetric.LAEQ);
        Float tempUpper = warnValueOf(thresholds, ThresholdMetric.TEMP_C);
        return new ComfortThresholds(noiseWarn, tempUpper, tempUpper == null ? null : comfortMinTempC);
    }

    private static Float warnValueOf(List<Threshold> thresholds, ThresholdMetric metric) {
        return thresholds.stream()
                .filter(threshold -> threshold.getMetric() == metric)
                .map(Threshold::getWarnValue)
                .findFirst()
                .orElse(null);
    }
}
