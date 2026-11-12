package com.pe.cloudapi.monitoring.domain.model.valueobjects;

import java.time.OffsetDateTime;

/** Estado fisico de la ultima muestra del minuto; el ADC no representa dBA. */
public record SensorMetrics(OffsetDateTime sampledAt, Boolean movement, Float distanceCm,
        Integer potentiometer, Boolean button1, Boolean button2, Boolean button3) {}
