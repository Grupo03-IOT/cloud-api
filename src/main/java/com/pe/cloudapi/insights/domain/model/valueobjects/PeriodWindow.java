package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Un {@link Period} resuelto a instantes en la zona de un local.
 *
 * @param from inicio, inclusive
 * @param to   fin, exclusive
 */
public record PeriodWindow(Period period, ZoneId zone, OffsetDateTime from, OffsetDateTime to) {

    /** Minutos del periodo. Un día con cambio de hora no tiene 1440. */
    public long minutes() {
        return Duration.between(from, to).toMinutes();
    }
}
