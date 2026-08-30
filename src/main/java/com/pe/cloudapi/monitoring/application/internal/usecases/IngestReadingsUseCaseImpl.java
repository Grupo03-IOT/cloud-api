package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.IngestReadingsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.IngestResult;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.RegisterDeviceCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.SyncDeviceStateCommand;
import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.ports.out.DeviceRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Guardar el lote de lecturas que sube el Edge.
 *
 * <p>Tres responsabilidades que no son evidentes leyendo el controlador:
 *
 * <ol>
 *   <li><strong>Deduplicar.</strong> El Edge reintenta cuando no confirma la
 *       subida, así que la entrega es <em>at-least-once</em>: en pruebas se
 *       observaron 543 envíos para 540 minutos reales. La base no tiene
 *       restricción {@code UNIQUE} sobre {@code (room_id, ts)} —es deuda
 *       técnica registrada—, así que se hace aquí.</li>
 *   <li><strong>Autoprovisionar.</strong> El Edge manda códigos de sala sin
 *       haber registrado nada previamente. Local, sala y dispositivo se dan de
 *       alta al vuelo la primera vez que aparecen.</li>
 *   <li><strong>Reflejar el estado del dispositivo,</strong> sin recalcular sus
 *       contadores: el cloud no ve la secuencia completa de lotes.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestReadingsUseCaseImpl implements IngestReadingsUseCase {

    private final SiteRepository sites;
    private final RoomRepository rooms;
    private final DeviceRepository devices;
    private final RoomReadingRepository readings;

    @Override
    @Transactional
    public IngestResult execute(IngestReadingsCommand command) {
        UUID siteId = resolveDefaultSite();
        int inserted = 0;
        int updated = 0;

        for (IngestReadingsCommand.Reading reading : command.readings()) {
            UUID roomId = resolveRoom(siteId, reading.roomCode());
            syncDevice(reading.device(), roomId);

            if (readings.findByRoomIdAndTs(roomId, reading.ts()).isPresent()) {
                updated++;
                continue;
            }

            readings.save(new RoomReading(reading.toRecordCommand(roomId)));
            inserted++;
        }

        return new IngestResult(command.readings().size(), inserted, updated);
    }

    /**
     * Devuelve el local al que se enganchan las salas.
     *
     * <p>No lo crea si no existe: dar de alta un local es una decisión de
     * negocio, y este caso de uso solo guarda lecturas. Inventarse uno haría
     * que el sistema se autoprovisionara con datos que nadie decidió, y que
     * quedaría un local llamado «Coworking» que nadie sabe de dónde salió.
     *
     * <p>Se falla en voz alta, que es recuperable, en vez de continuar con un
     * dato inventado, que no lo es.
     */
    private UUID resolveDefaultSite() {
        return sites.findDefault()
                .map(Site::getId)
                .orElseThrow(MonitoringError.NO_SITE_AVAILABLE::with);
    }

    /**
     * Resuelve el código de sala que trae el firmware, dándola de alta sin
     * clasificar si es la primera vez que aparece.
     */
    private UUID resolveRoom(UUID siteId, String roomCode) {
        return rooms.findBySiteIdAndCode(siteId, roomCode)
                .map(Room::getId)
                .orElseGet(() -> {
                    log.info("Sala desconocida '{}': alta automática, sin clasificar", roomCode);
                    Room created = rooms.save(new Room(
                            CreateRoomCommand.autoRegistered(siteId, roomCode)));
                    return created.getId();
                });
    }

    /**
     * Refleja el estado del dispositivo que reporta por la sala. No hace nada
     * si la lectura no lo declara.
     */
    private void syncDevice(IngestReadingsCommand.Reading.Device input, UUID roomId) {
        if (input == null || input.code() == null || input.code().isBlank()) {
            return;
        }
        SyncDeviceStateCommand command = new SyncDeviceStateCommand(
                input.code(),
                roomId,
                input.fwVersion(),
                input.lastSeen(),
                input.lastSeq(),
                input.lostBatches() == null ? 0L : input.lostBatches());

        Device device = devices.findByCode(command.code())
                .orElseGet(() -> {
                    log.info("Dispositivo desconocido '{}': alta automática", command.code());
                    return new Device(new RegisterDeviceCommand(command.code(), roomId));
                });
        device.handle(command);
        devices.save(device);
    }
}
