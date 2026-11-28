package com.pe.cloudapi.monitoring.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.ThresholdMetric;
import com.pe.cloudapi.alerting.interfaces.acl.AlertingContextFacade;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ComfortThresholds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Comfort limits taken from alerting thresholds")
class ExternalAlertingServiceTest {

    private final AlertingContextFacade alerting = mock(AlertingContextFacade.class);
    private final ExternalAlertingService service = new ExternalAlertingService(alerting, 20f);
    private final UUID roomType = UUID.randomUUID();

    @Test
    @DisplayName("uses the warn values, never the critical ones, and the configured lower limit")
    void warnValues() {
        when(alerting.thresholdsOf(roomType)).thenReturn(List.of(
                threshold(ThresholdMetric.L10, 70f), threshold(ThresholdMetric.LAEQ, 55f),
                threshold(ThresholdMetric.TEMP_C, 26f)));

        assertThat(service.comfortThresholdsOf(roomType)).isEqualTo(new ComfortThresholds(55f, 26f, 20f));
    }

    @Test
    @DisplayName("without a temperature threshold there is no temperature limit at all")
    void noTemperatureThreshold() {
        when(alerting.thresholdsOf(roomType)).thenReturn(List.of(threshold(ThresholdMetric.LAEQ, 55f)));

        assertThat(service.comfortThresholdsOf(roomType)).isEqualTo(new ComfortThresholds(55f, null, null));
    }

    private Threshold threshold(ThresholdMetric metric, float warn) {
        return Threshold.builder().roomTypeId(roomType).metric(metric).warnValue(warn)
                .criticalValue(warn + 10).sustainedMinutes(1).enabled(true).build();
    }
}
