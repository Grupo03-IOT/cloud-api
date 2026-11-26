package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Lo que un miembro dice sentir en una sala, junto con lo que marcaba el
 * sensor en ese momento (US27).
 *
 * <p>Poner las dos cosas juntas es lo que permite al administrador ver «dice
 * que hace calor y el sensor marca 24,9 °C» sin cruzar datos a mano (US28).
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class DiscomfortReport {

    /** Un usuario no puede repetir el mismo motivo en la misma sala antes de esto. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(10);

    /** Una lectura más vieja que esto ya no describe lo que siente el miembro. */
    public static final Duration READING_MAX_AGE = Duration.ofMinutes(10);

    private final UUID id;
    private final UUID roomId;
    private final UUID userId;
    private final DiscomfortCategory category;
    private final String comment;
    private final OffsetDateTime createdAt;
    private final ReadingAtReport reading;

    /**
     * Registra un reporte nuevo.
     *
     * @param command  sala, usuario, motivo y comentario
     * @param previous último reporte del mismo usuario, sala y motivo, si hay
     * @param latest   última lectura de la sala, si hay
     * @param now      momento del reporte
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_DISCOMFORT_TOO_FREQUENT} si el anterior tiene
     *         menos de diez minutos
     */
    public DiscomfortReport(FileDiscomfortReportCommand command, Optional<DiscomfortReport> previous,
                            Optional<ReadingAtReport> latest, OffsetDateTime now) {
        previous.filter(last -> last.blocksAnotherAt(now)).ifPresent(last -> {
            throw MonitoringError.DISCOMFORT_TOO_FREQUENT.with(last.getCategory(), last.getCreatedAt());
        });
        this.id = null;
        this.roomId = command.roomId();
        this.userId = command.userId();
        this.category = command.category();
        this.comment = command.comment();
        this.createdAt = now;
        this.reading = latest.filter(reading -> isRecent(reading, now)).orElse(null);
    }

    /**
     * @return {@code true} si este reporte impide otro igual en {@code at}
     */
    public boolean blocksAnotherAt(OffsetDateTime at) {
        return at.isBefore(createdAt.plus(MIN_INTERVAL));
    }

    private static boolean isRecent(ReadingAtReport reading, OffsetDateTime now) {
        return reading.ts() != null && !reading.ts().isBefore(now.minus(READING_MAX_AGE));
    }
}
