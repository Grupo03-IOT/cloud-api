package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.domain.model.valueobjects.SensorMetrics;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.SensorResource;

public final class SensorResourceAssembler {
    private SensorResourceAssembler() {}

    public static SensorMetrics toDomain(SensorResource s) {
        return s == null ? null : new SensorMetrics(s.sampledAt(), s.movement(), s.distanceCm(),
                s.potentiometer(), s.button1(), s.button2(), s.button3());
    }

    public static SensorResource toResource(SensorMetrics s) {
        return s == null ? null : new SensorResource(s.sampledAt(), s.movement(), s.distanceCm(),
                s.potentiometer(), s.button1(), s.button2(), s.button3());
    }
}
