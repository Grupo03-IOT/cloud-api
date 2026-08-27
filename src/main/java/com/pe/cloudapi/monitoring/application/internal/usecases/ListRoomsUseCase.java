package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRooms;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.repositories.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.repositories.RoomRepository;
import com.pe.cloudapi.monitoring.domain.repositories.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Listar las salas activas con su última lectura.
 *
 * <p>Es la consulta principal de la aplicación móvil: con una sola llamada se
 * sabe qué salas hay, cómo están y cuán reciente es ese dato.
 *
 * <p>Mientras el producto atienda un solo local, se resuelve contra el local
 * por defecto en vez de exigir que quien llama lo indique. Si no hay ninguno
 * todavía, la lista sale vacía en vez de fallar: es el estado normal antes de
 * que llegue la primera lectura.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRoomsUseCase implements ListRooms {

    private final SiteRepository sites;
    private final RoomRepository rooms;
    private final RoomReadingRepository readings;

    @Override
    public List<RoomSnapshot> execute() {
        return sites.findDefault()
                .map(site -> rooms.findActiveBySiteId(site.getId()).stream()
                        .map(room -> new RoomSnapshot(room, readings.findLatest(room.getId())))
                        .toList())
                .orElseGet(List::of);
    }
}
