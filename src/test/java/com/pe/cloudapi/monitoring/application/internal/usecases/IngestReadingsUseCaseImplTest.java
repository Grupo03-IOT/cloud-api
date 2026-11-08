package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Room;
import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.ports.out.*;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngestReadingsUseCaseImplTest {
    final SiteRepository sites = mock(SiteRepository.class);
    final RoomRepository rooms = mock(RoomRepository.class);
    final DeviceRepository devices = mock(DeviceRepository.class);
    final RoomReadingRepository readings = mock(RoomReadingRepository.class);
    final IngestReadingsUseCaseImpl useCase =
            new IngestReadingsUseCaseImpl(sites, rooms, devices, readings);
    final UUID siteId = UUID.randomUUID(), roomId = UUID.randomUUID();
    final OffsetDateTime ts = OffsetDateTime.parse("2026-01-01T12:00:00Z");

    IngestReadingsCommand command(String site) {
        var reading = new IngestReadingsCommand.Reading("sala-01", ts, 60, null,
                null, new Climate(25f, 50f), null, null, null);
        return new IngestReadingsCommand(site, List.of(reading));
    }

    void givenSiteAndRoom() {
        when(sites.findByCode("site-2")).thenReturn(Optional.of(
                Site.builder().id(siteId).code("site-2").build()));
        when(rooms.findBySiteIdAndCode(siteId, "sala-01")).thenReturn(Optional.of(
                Room.builder().id(roomId).build()));
    }

    @Test
    void usesTheExplicitSiteRatherThanTheOldest() {
        givenSiteAndRoom();
        when(readings.findByRoomIdAndTs(roomId, ts)).thenReturn(Optional.empty());
        var result = useCase.execute(command("site-2"));
        assertEquals(1, result.inserted());
        verify(sites).lockById(siteId);
        verify(sites, never()).findDefault();
        verify(rooms).findBySiteIdAndCode(siteId, "sala-01");
    }

    @Test
    void rejectsAmbiguousLegacyBatch() {
        when(sites.findAll()).thenReturn(List.of(
                Site.builder().id(siteId).build(), Site.builder().id(UUID.randomUUID()).build()));
        assertThrows(DomainException.class, () -> useCase.execute(command(null)));
        verifyNoInteractions(rooms, devices, readings);
    }

    @Test
    void acceptsLegacyBatchForASingleSite() {
        givenSiteAndRoom();
        when(sites.findAll()).thenReturn(List.of(Site.builder().id(siteId).build()));
        assertEquals(1, useCase.execute(command(null)).inserted());
    }

    @Test
    void rejectsUnknownSiteWithoutCreatingRooms() {
        when(sites.findByCode("site-2")).thenReturn(Optional.empty());
        assertThrows(DomainException.class, () -> useCase.execute(command("site-2")));
        verifyNoInteractions(rooms, readings);
    }

    @Test
    void replacesMetricsOfRecalculatedMinuteUsingTheExistingIdentity() {
        givenSiteAndRoom();
        UUID readingId = UUID.randomUUID();
        when(readings.findByRoomIdAndTs(roomId, ts)).thenReturn(Optional.of(
                RoomReading.builder().id(readingId).build()));
        var result = useCase.execute(command("site-2"));
        assertEquals(0, result.inserted());
        assertEquals(1, result.updated());
        var saved = ArgumentCaptor.forClass(RoomReading.class);
        verify(readings).save(saved.capture());
        assertEquals(readingId, saved.getValue().getId());
        assertEquals(25f, saved.getValue().getClimate().tempC());
    }
}
