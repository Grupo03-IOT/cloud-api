package com.pe.cloudapi.monitoring.interfaces.rest.resources;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

/** La distancia puede faltar cuando el HC-SR04 no recibe eco. */
public record SensorResource(
        @NotNull OffsetDateTime sampledAt,
        @NotNull Boolean movement,
        @DecimalMin("0.0") @DecimalMax("10000.0") Float distanceCm,
        @NotNull @Min(0) @Max(4095) Integer potentiometer,
        @NotNull Boolean button1, @NotNull Boolean button2, @NotNull Boolean button3) {}
