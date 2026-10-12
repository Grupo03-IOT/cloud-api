package com.pe.cloudapi.insights.domain.model.valueobjects;

import java.util.List;
import java.util.UUID;

/**
 * @param hours siempre 24, de las 0 a las 23
 */
public record HourlyProfile(UUID roomId, PeriodWindow window, List<HourSlot> hours) {}
