package com.pe.cloudapi.alerting.application.internal.usecases;

import com.pe.cloudapi.alerting.application.internal.ports.in.ResolveRoomThresholdsUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomThresholds;
import com.pe.cloudapi.alerting.domain.model.entities.Threshold;
import com.pe.cloudapi.alerting.domain.model.valueobjects.RoomProfile;
import com.pe.cloudapi.alerting.domain.ports.out.RoomProfileProvider;
import com.pe.cloudapi.alerting.domain.ports.out.ThresholdRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolver el umbral de cada sala.
 *
 * <p>Los umbrales se configuran por <strong>tipo</strong> de sala y se aplican
 * a <strong>salas</strong> concretas. Traducir de uno a otro es lo que hace
 * este caso de uso, y por eso vive aquí y no en el Edge: el Edge no sabe que
 * los tipos existen.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResolveRoomThresholdsUseCaseImpl implements ResolveRoomThresholdsUseCase {

    private final RoomProfileProvider rooms;
    private final ThresholdRepository thresholds;

    @Override
    public List<RoomThresholds> execute() {
        // Una consulta por tipo, no por sala: los tipos son cuatro y las salas
        // pueden ser cincuenta.
        Map<UUID, List<Threshold>> porTipo = new HashMap<>();

        return rooms.rooms().stream()
                .map(room -> new RoomThresholds(room.code(), thresholdsFor(room, porTipo)))
                .toList();
    }

    private List<Threshold> thresholdsFor(RoomProfile room, Map<UUID, List<Threshold>> cache) {
        if (!room.isClassified()) {
            return List.of();
        }
        return cache.computeIfAbsent(room.roomTypeId(), thresholds::findEnabledByRoomTypeId);
    }
}
