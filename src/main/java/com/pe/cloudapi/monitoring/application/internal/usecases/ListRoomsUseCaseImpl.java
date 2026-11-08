package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.RoomSnapshot;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomTypeRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listar las salas activas con su última lectura.
 *
 * <p>Es la consulta principal de la aplicación móvil: con una sola llamada se
 * sabe qué salas hay, cómo están y cuán reciente es ese dato.
 *
 * <p>Incluye todos los locales activos. Cada recurso conserva su site_id para
 * que las aplicaciones puedan filtrar por local sin ocultar los demas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRoomsUseCaseImpl implements ListRoomsUseCase {

    private final SiteRepository sites;
    private final RoomRepository rooms;
    private final RoomReadingRepository readings;
    private final RoomTypeRepository roomTypes;

    @Override
    public List<RoomSnapshot> execute() {
        return sites.findAll().stream()
                .flatMap(site -> {
                    Map<UUID, RoomType> typesById = roomTypes.findAllBySiteId(site.getId()).stream()
                            .collect(Collectors.toMap(RoomType::getId, Function.identity()));
                    return rooms.findActiveBySiteId(site.getId()).stream()
                            .map(room -> new RoomSnapshot(room, readings.findLatest(room.getId()),
                                    Optional.ofNullable(room.getRoomTypeId()).map(typesById::get)));
                }).toList();
    }
}
