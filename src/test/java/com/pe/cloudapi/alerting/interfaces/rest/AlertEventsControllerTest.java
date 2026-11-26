package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.RecordAlertEventsUseCase;
import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.interfaces.rest.transform.AlertResourceAssembler;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlertEventsController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({AlertEventsController.class, AlertResourceAssembler.class})
@DisplayName("POST /api/v1/alerts/events (Edge alert upload)")
class AlertEventsControllerTest extends WebLayerTestSupport {

    private static final String BATCH = """
            {"events":[{"source_key":"edge-lima-centro:42","room_id":"sala-03",
            "rule":"sustained_noise","severity":"warning",
            "message":"Noise above 60 dBA for 2 min","value":66.4,
            "opened_at":"2026-11-08T14:10:00Z","closed_at":null}]}""";

    @MockitoBean private RecordAlertEventsUseCase recordAlertEvents;

    @Test
    @DisplayName("200: an Edge with alerts:write uploads a batch")
    void edgeUploads() throws Exception {
        givenApiKeyWith(Scope.ALERTS_WRITE);
        when(recordAlertEvents.execute(anyList())).thenReturn(1);

        mockMvc.perform(post("/api/v1/alerts/events").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(BATCH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(1));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RecordAlertEventCommand>> events = ArgumentCaptor.forClass(List.class);
        verify(recordAlertEvents).execute(events.capture());
        RecordAlertEventCommand event = events.getValue().getFirst();
        assertThat(event.sourceKey()).isEqualTo("edge-lima-centro:42");
        assertThat(event.roomCode()).isEqualTo("sala-03");
        assertThat(event.severity()).isEqualTo(AlertSeverity.WARNING);
        assertThat(event.openedAt()).isEqualTo(OffsetDateTime.parse("2026-11-08T14:10:00Z"));
        assertThat(event.closedAt()).isNull();
    }

    @Test
    @DisplayName("401: no credential at all")
    void unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/alerts/events")
                        .contentType(MediaType.APPLICATION_JSON).content(BATCH))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(recordAlertEvents);
    }

    @Test
    @DisplayName("403: a key that may only upload readings cannot upload alerts")
    void wrongScope() throws Exception {
        givenApiKeyWith(Scope.READINGS_WRITE);

        mockMvc.perform(post("/api/v1/alerts/events").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(BATCH))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recordAlertEvents);
    }

    @Test
    @DisplayName("403: a person, even an administrator, cannot upload alert events")
    void adminCannotUpload() throws Exception {
        mockMvc.perform(post("/api/v1/alerts/events").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(BATCH))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recordAlertEvents);
    }

    @Test
    @DisplayName("404: an unknown room is reported with MONITORING_ROOM_NOT_FOUND")
    void unknownRoom() throws Exception {
        givenApiKeyWith(Scope.ALERTS_WRITE);
        when(recordAlertEvents.execute(anyList())).thenThrow(MonitoringError.ROOM_NOT_FOUND.with("sala-03"));

        mockMvc.perform(post("/api/v1/alerts/events").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(BATCH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MONITORING_ROOM_NOT_FOUND"));
    }

    @Test
    @DisplayName("400: empty batch, unknown severity, bad rule or closing before opening")
    void invalidBatches() throws Exception {
        givenApiKeyWith(Scope.ALERTS_WRITE);

        for (String body : List.of(
                "{\"events\":[]}",
                BATCH.replace("\"warning\"", "\"urgent\""),
                BATCH.replace("sustained_noise", "Sustained Noise"),
                BATCH.replace("\"closed_at\":null", "\"closed_at\":\"2026-11-08T14:00:00Z\""),
                BATCH.replace("\"opened_at\":\"2026-11-08T14:10:00Z\",", ""))) {
            mockMvc.perform(post("/api/v1/alerts/events").header("X-API-Key", "key")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(recordAlertEvents);
    }

    private void givenApiKeyWith(Scope scope) {
        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-lima-centro", "hash", Set.of(scope))));
    }
}
