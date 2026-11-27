package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.AcknowledgeAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.ports.out.AlertRepository;
import com.pe.cloudapi.alerting.domain.ports.out.RoomDirectory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Reconocer una alerta abierta (US21). La regla de estado la decide el agregado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcknowledgeAlertUseCaseImpl implements AcknowledgeAlertUseCase {

    private final AlertRepository alerts;
    private final RoomDirectory roomDirectory;

    @Override
    @Transactional
    public RoomAlert execute(AcknowledgeAlertCommand command) {
        Alert alert = alerts.findById(command.alertId())
                .orElseThrow(() -> AlertingError.ALERT_NOT_FOUND.with(command.alertId()));
        alert.acknowledge(command, OffsetDateTime.now(ZoneOffset.UTC));
        Alert saved = alerts.save(alert);
        log.info("alert.acknowledged id={} by={}", saved.getId(), saved.getAcknowledgedBy());
        return new RoomAlert(saved, roomDirectory.roomCodes().get(saved.getRoomId()));
    }
}
