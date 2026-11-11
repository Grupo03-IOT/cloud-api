package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.IngestReadingsUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomsUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomReadingRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.RoomRepository;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReadingIngestionIntegrationTest {
    @Autowired IngestReadingsUseCase ingest;
    @Autowired ListRoomsUseCase listRooms;
    @Autowired SiteRepository sites;
    @Autowired RoomRepository rooms;
    @Autowired RoomReadingRepository readings;

    IngestReadingsCommand command(String site, float temperature) {
        var reading = new IngestReadingsCommand.Reading("sala-01",
                OffsetDateTime.parse("2026-01-01T12:00:00Z"), 60,
                null, null, new Climate(temperature, 50f), null, null, null);
        return new IngestReadingsCommand(site, List.of(reading));
    }

    @Test
    void separatesSitesAndPersistsALateCorrectionWithoutAnotherReading() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var first = sites.save(new Site(new CreateSiteCommand(
                "test-a-" + suffix, "Prueba A", "Laboratorio", "America/Lima")));
        var second = sites.save(new Site(new CreateSiteCommand(
                "test-b-" + suffix, "Prueba B", "Laboratorio", "America/Lima")));
        ingest.execute(command(first.getCode(), 20));
        ingest.execute(command(second.getCode(), 21));
        var roomA = rooms.findBySiteIdAndCode(first.getId(), "sala-01").orElseThrow();
        var roomB = rooms.findBySiteIdAndCode(second.getId(), "sala-01").orElseThrow();
        assertNotEquals(roomA.getId(), roomB.getId());
        var previous = readings.findLatest(roomB.getId()).orElseThrow();
        var result = ingest.execute(command(second.getCode(), 27));
        assertEquals(1, result.updated());
        var corrected = readings.findLatest(roomB.getId()).orElseThrow();
        assertEquals(previous.getId(), corrected.getId());
        assertEquals(27f, corrected.getClimate().tempC());
        assertEquals(20f, readings.findLatest(roomA.getId()).orElseThrow().getClimate().tempC());
        assertEquals(1, readings.findInRange(roomB.getId(), previous.getTs().minusMinutes(1),
                previous.getTs().plusMinutes(1)).size());
        var visible = listRooms.execute();
        assertTrue(visible.stream().anyMatch(snapshot -> snapshot.room().getId().equals(roomA.getId())));
        assertTrue(visible.stream().anyMatch(snapshot -> snapshot.room().getId().equals(roomB.getId())));
    }
}
