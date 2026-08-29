package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.ListThresholds;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.queries.ListThresholdsQuery;
import com.pe.cloudapi.alerting.domain.repositories.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar los umbrales activos de un tipo de sala.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListThresholdsUseCase implements ListThresholds {

    private final ThresholdRepository thresholds;

    @Override
    public List<Threshold> execute(ListThresholdsQuery query) {
        return thresholds.findEnabledByRoomTypeId(query.roomTypeId());
    }
}
