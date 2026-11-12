package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers.RoomReadingMapper;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.ReadingResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.SensorResource;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.ReadingResourceAssembler;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.ReadingResourceAssemblerFromDomain;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalSensorsTest {
    final OffsetDateTime sampledAt = OffsetDateTime.parse("2026-11-11T12:00:58Z");

    ReadingResource reading(SensorResource sensors) {
        return new ReadingResource("sala-01", sampledAt.withSecond(0), 60, null,
                null, new ReadingResource.Climate(28.6f, 75.6f), null,
                new ReadingResource.Occupancy(100f, 0), new ReadingResource.Quality(30, 30),
                null, sensors);
    }

    @Test
    void physicalReadingsSurviveTheDomainAndPersistenceMappingWithoutInventingNoise() {
        var sensors = new SensorResource(sampledAt, true, 154.14f, 873, false, true, false);
        var command = new ReadingResourceAssembler().toCommand("site-1", List.of(reading(sensors)));
        var domain = new RoomReading(command.readings().getFirst().toRecordCommand(UUID.randomUUID()));
        var mapper = new RoomReadingMapper();
        var restored = mapper.toDomain(mapper.toEntity(domain));
        var result = new ReadingResourceAssemblerFromDomain().toResource(restored, "sala-01");
        assertEquals(sensors, result.sensors());
        assertEquals(28.6f, result.climate().tempC());
        assertNull(result.acoustic().laeq());
        assertTrue(restored.isReliable());
    }

    @Test
    void missingEchoIsAllowedAndAdcAndButtonTypesRemainValidated() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(reading(new SensorResource(
                    sampledAt, false, null, 0, false, false, false))).isEmpty());
            assertFalse(validator.validate(reading(new SensorResource(
                    sampledAt, true, -1f, 4096, null, false, false))).isEmpty());
        }
    }

    @Test
    void legacyReadingsDoNotGainAnEmptySensorSnapshot() {
        var resource = reading(null);
        var command = new ReadingResourceAssembler().toCommand("site-1", List.of(resource));
        var mapper = new RoomReadingMapper();
        var domain = new RoomReading(command.readings().getFirst().toRecordCommand(UUID.randomUUID()));
        assertNull(mapper.toDomain(mapper.toEntity(domain)).getSensors());
    }
}
