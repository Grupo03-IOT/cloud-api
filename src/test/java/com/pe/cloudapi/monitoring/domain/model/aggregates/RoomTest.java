package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.ClassifyRoomCommand;
import com.pe.cloudapi.monitoring.domain.model.commands.CreateRoomCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Room aggregate")
class RoomTest {

    private final UUID siteId = UUID.randomUUID();

    @Test
    @DisplayName("an auto-registered room is born active, unclassified and named after its code")
    void autoRegisteredRoomIsUnclassified() {
        Room room = new Room(CreateRoomCommand.autoRegistered(siteId, "sala-01"));

        assertThat(room.getSiteId()).isEqualTo(siteId);
        assertThat(room.getDisplayName()).isEqualTo("sala-01");
        assertThat(room.isActive()).isTrue();
        assertThat(room.isClassified()).isFalse();
    }

    @Test
    @DisplayName("becomes classified once a room type is assigned")
    void classifyAssignsRoomType() {
        Room room = new Room(CreateRoomCommand.autoRegistered(siteId, "sala-01"));
        UUID roomTypeId = UUID.randomUUID();

        room.handle(new ClassifyRoomCommand(room.getId(), roomTypeId));

        assertThat(room.isClassified()).isTrue();
        assertThat(room.getRoomTypeId()).isEqualTo(roomTypeId);
    }
}
