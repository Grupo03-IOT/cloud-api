package com.pe.cloudapi.insights.domain.model.valueobjects;

import com.pe.cloudapi.insights.domain.model.errors.InsightsError;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Un mes, una semana ISO o un día, todavía sin zona horaria.
 *
 * <p>Se escribe {@code 2026-10}, {@code 2026-W42} o {@code 2026-10-12}, y se
 * convierte en instantes con {@link #in(ZoneId)}.
 *
 * @param granularity si es un día, una semana o un mes
 * @param start       primer día del periodo; el lunes, si es una semana
 */
public record Period(Granularity granularity, LocalDate start) {

    public enum Granularity { DAY, WEEK, MONTH }

    private static final Pattern DAY = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern WEEK = Pattern.compile("(\\d{4})-W(\\d{2})");
    private static final Pattern MONTH = Pattern.compile("\\d{4}-\\d{2}");

    public Period {
        Objects.requireNonNull(granularity, "granularity");
        Objects.requireNonNull(start, "start");
    }

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code INSIGHTS_PERIOD_INVALID} si no es ninguno de los tres formatos
     *         o no es una fecha que exista
     */
    public static Period parse(String text) {
        String value = text == null ? "" : text.trim();
        try {
            if (DAY.matcher(value).matches()) {
                return new Period(Granularity.DAY, LocalDate.parse(value));
            }
            Matcher week = WEEK.matcher(value);
            if (week.matches()) {
                return ofWeek(Integer.parseInt(week.group(1)), Integer.parseInt(week.group(2)), text);
            }
            if (MONTH.matcher(value).matches()) {
                return new Period(Granularity.MONTH, YearMonth.parse(value).atDay(1));
            }
        } catch (DateTimeException ex) {
            throw InsightsError.PERIOD_INVALID.with(text);
        }
        throw InsightsError.PERIOD_INVALID.with(text);
    }

    private static Period ofWeek(int weekBasedYear, int week, String text) {
        LocalDate anchor = LocalDate.of(weekBasedYear, 1, 4);
        if (!IsoFields.WEEK_OF_WEEK_BASED_YEAR.rangeRefinedBy(anchor).isValidValue(week)) {
            throw InsightsError.PERIOD_INVALID.with(text);
        }
        return new Period(Granularity.WEEK,
                anchor.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week).with(DayOfWeek.MONDAY));
    }

    /** El primer día que ya no pertenece al periodo. */
    public LocalDate end() {
        return switch (granularity) {
            case DAY -> start.plusDays(1);
            case WEEK -> start.plusWeeks(1);
            case MONTH -> start.plusMonths(1);
        };
    }

    /** El periodo inmediatamente anterior, de la misma granularidad. */
    public Period previous() {
        return switch (granularity) {
            case DAY -> new Period(granularity, start.minusDays(1));
            case WEEK -> new Period(granularity, start.minusWeeks(1));
            case MONTH -> new Period(granularity, start.minusMonths(1));
        };
    }

    /** El periodo escrito como se pide: {@code 2026-10}, {@code 2026-W42} o {@code 2026-10-12}. */
    public String label() {
        return switch (granularity) {
            case DAY -> start.toString();
            case WEEK -> String.format("%d-W%02d",
                    start.get(IsoFields.WEEK_BASED_YEAR), start.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MONTH -> YearMonth.from(start).toString();
        };
    }

    /**
     * El periodo en la zona del local: empieza a las 00:00 de su primer día y
     * termina, sin incluirlo, a las 00:00 del día siguiente al último.
     */
    public PeriodWindow in(ZoneId zone) {
        return new PeriodWindow(this, zone,
                start.atStartOfDay(zone).toOffsetDateTime(),
                end().atStartOfDay(zone).toOffsetDateTime());
    }
}
