package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.GetAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.queries.GetAlertQuery;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Obtener una alerta con su sala y quién la reconoció.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAlertUseCaseImpl implements GetAlertUseCase {

    private final AlertRepository alerts;
    private final RoomAlerts roomAlerts;

    @Override
    public RoomAlert execute(GetAlertQuery query) {
        Alert alert = alerts.findById(query.alertId())
                .orElseThrow(() -> AlertingError.ALERT_NOT_FOUND.with(query.alertId()));
        return roomAlerts.of(alert);
    }
}
