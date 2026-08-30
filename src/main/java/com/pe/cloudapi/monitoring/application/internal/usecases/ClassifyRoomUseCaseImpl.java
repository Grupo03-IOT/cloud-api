package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ClassifyRoomUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.commands.ClassifyRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asignar un tipo a una sala.
 *
 * <p>Comprueba que el tipo pertenezca al mismo local que la sala. Sin esa
 * comprobación se podría clasificar una sala con el tipo de otro coworking, y
 * acabaría evaluándose contra umbrales que nadie configuró para ella.
 *
 * <p>Devuelve la sala ya clasificada junto a su última lectura, la misma forma
 * que las consultas: así quien responde no tiene que volver a pedirla.
 */
@Service
@RequiredArgsConstructor
public class ClassifyRoomUseCaseImpl implements ClassifyRoomUseCase {

    private final RoomRepository rooms;
    private final RoomTypeRepository roomTypes;
    private final RoomReadingRepository readings;

    @Override
    @Transactional
    public RoomSnapshot execute(ClassifyRoomCommand command) {
        Room room = rooms.findById(command.roomId())
                .orElseThrow(() -> MonitoringError.ROOM_NOT_FOUND.with(command.roomId()));
        RoomType roomType = roomTypes.findById(command.roomTypeId())
                .orElseThrow(() -> MonitoringError.ROOM_TYPE_NOT_FOUND.with(command.roomTypeId()));

        if (!roomType.getSiteId().equals(room.getSiteId())) {
            throw MonitoringError.ROOM_TYPE_FROM_ANOTHER_SITE
                    .with(command.roomTypeId(), command.roomId());
        }

        room.handle(command);
        Room classified = rooms.save(room);
        return new RoomSnapshot(classified, readings.findLatest(classified.getId()));
    }
}
