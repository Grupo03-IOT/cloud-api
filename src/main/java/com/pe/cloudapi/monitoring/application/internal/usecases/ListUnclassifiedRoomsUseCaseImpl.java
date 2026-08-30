package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListUnclassifiedRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar las salas que aparecieron solas y nadie ha clasificado todavía.
 *
 * <p>Una sala se da de alta automáticamente cuando un dispositivo desconocido
 * empieza a reportar por ella. Hasta que un administrador le asigna un tipo no
 * tiene umbrales aplicables, así que esta es su bandeja de trabajo.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUnclassifiedRoomsUseCaseImpl implements ListUnclassifiedRoomsUseCase {

    private final RoomRepository rooms;
    private final RoomReadingRepository readings;

    @Override
    public List<RoomSnapshot> execute() {
        return rooms.findUnclassified().stream()
                .map(room -> new RoomSnapshot(room, readings.findLatest(room.getId())))
                .toList();
    }
}
