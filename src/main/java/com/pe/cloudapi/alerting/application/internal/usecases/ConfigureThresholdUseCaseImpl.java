package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.ConfigureThresholdUseCase;
import com.pe.cloudapi.alerting.domain.model.commands.ConfigureThresholdCommand;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.ports.out.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fijar el umbral de una métrica para un tipo de sala.
 */
@Service
@RequiredArgsConstructor
public class ConfigureThresholdUseCaseImpl implements ConfigureThresholdUseCase {

    private final ThresholdRepository thresholds;

    @Override
    @Transactional
    public Threshold execute(ConfigureThresholdCommand command) {
        return thresholds
                .findByRoomTypeIdAndMetric(command.roomTypeId(), command.metric())
                .map(existing -> {
                    existing.handle(command);
                    return thresholds.save(existing);
                })
                .orElseGet(() -> thresholds.save(new Threshold(command)));
    }
}
