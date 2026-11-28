package com.pe.cloudapi.alerting.interfaces.rest;

import com.pe.cloudapi.alerting.application.internal.ports.in.AcknowledgeAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.GetAlertUseCase;
import com.pe.cloudapi.alerting.application.internal.ports.in.ListAlertsUseCase;
import com.pe.cloudapi.alerting.application.internal.results.RoomAlert;
import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;
import com.pe.cloudapi.alerting.domain.model.commands.AcknowledgeAlertCommand;
import com.pe.cloudapi.alerting.domain.model.errors.AlertingError;
import com.pe.cloudapi.alerting.domain.model.queries.ListAlertsQuery;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertSeverity;
import com.pe.cloudapi.alerting.domain.model.valueobjects.AlertStatus;
import com.pe.cloudapi.alerting.interfaces.rest.transform.AlertResourceAssembler;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlertsController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({AlertsController.class, AlertResourceAssembler.class})
@DisplayName("/api/v1/alerts (administrator)")
class AlertsControllerTest extends WebLayerTestSupport {

    private static final String ACK = "{\"corrective_action\":\"Asked for silence in room 3\"}";

    @MockitoBean private ListAlertsUseCase listAlerts;
    @MockitoBean private GetAlertUseCase getAlert;
    @MockitoBean private AcknowledgeAlertUseCase acknowledgeAlert;

    private final UUID alertId = UUID.randomUUID();
    private final UUID roomId = UUID.randomUUID();

    @Test
    @DisplayName("200: an administrator lists alerts with the full resource, nulls included")
    void adminLists() throws Exception {
        when(listAlerts.execute(any())).thenReturn(List.of(new RoomAlert(openAlert(), "sala-03", null)));

        mockMvc.perform(get("/api/v1/alerts").with(admin())
                        .param("status", "open").param("room_id", roomId.toString())
                        .param("from", "2026-11-08T00:00:00Z").param("to", "2026-11-09T00:00:00Z")
                        .param("has_action", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(alertId.toString()))
                .andExpect(jsonPath("$[0].room_code").value("sala-03"))
                .andExpect(jsonPath("$[0].severity").value("warning"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].closed_at").isEmpty())
                .andExpect(jsonPath("$[0].corrective_action").isEmpty())
                .andExpect(content().string(containsString("\"acknowledged_by_name\":null")));

        ArgumentCaptor<ListAlertsQuery> query = ArgumentCaptor.forClass(ListAlertsQuery.class);
        verify(listAlerts).execute(query.capture());
        assertThat(query.getValue().status()).isEqualTo(AlertStatus.OPEN);
        assertThat(query.getValue().roomId()).isEqualTo(roomId);
        assertThat(query.getValue().hasAction()).isFalse();
    }

    @Test
    @DisplayName("400: an unknown status or an inverted range")
    void invalidFilters() throws Exception {
        mockMvc.perform(get("/api/v1/alerts").with(admin()).param("status", "PENDING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALERTING_UNKNOWN_ALERT_STATUS"));
        mockMvc.perform(get("/api/v1/alerts").with(admin())
                        .param("from", "2026-11-09T00:00:00Z").param("to", "2026-11-08T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALERTING_RANGE_INVERTED"));
    }

    @Test
    @DisplayName("401: no credential")
    void unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/alerts")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/alerts/" + alertId + "/acknowledge")
                        .contentType(MediaType.APPLICATION_JSON).content(ACK))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403: a MEMBER cannot list, read or acknowledge alerts")
    void memberForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/alerts").with(member())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/alerts/" + alertId).with(member())).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/alerts/" + alertId + "/acknowledge").with(member())
                        .contentType(MediaType.APPLICATION_JSON).content(ACK))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listAlerts, getAlert, acknowledgeAlert);
    }

    @Test
    @DisplayName("403: an Edge key, even with alerts:write, cannot read alerts")
    void machineForbidden() throws Exception {
        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-lima-centro", "hash", Set.of(Scope.ALERTS_WRITE))));

        mockMvc.perform(get("/api/v1/alerts").header("X-API-Key", "key"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listAlerts);
    }

    @Test
    @DisplayName("200 / 404: an administrator reads one alert")
    void getOne() throws Exception {
        when(getAlert.execute(any())).thenReturn(new RoomAlert(openAlert(), "sala-03", null));
        mockMvc.perform(get("/api/v1/alerts/" + alertId).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rule").value("sustained_noise"));

        when(getAlert.execute(any())).thenThrow(AlertingError.ALERT_NOT_FOUND.with(alertId));
        mockMvc.perform(get("/api/v1/alerts/" + alertId).with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ALERTING_ALERT_NOT_FOUND"));
    }

    @Test
    @DisplayName("200: acknowledging takes the user from the token, never from the body")
    void acknowledges() throws Exception {
        UUID adminId = UUID.randomUUID();
        Alert acknowledged = openAlert().toBuilder().status(AlertStatus.ACKNOWLEDGED)
                .acknowledgedBy(adminId).acknowledgedAt(OffsetDateTime.parse("2026-11-08T14:15:00Z"))
                .correctiveAction("Asked for silence in room 3").build();
        when(acknowledgeAlert.execute(any())).thenReturn(new RoomAlert(acknowledged, "sala-03", "Ana Torres"));

        mockMvc.perform(patch("/api/v1/alerts/" + alertId + "/acknowledge")
                        .with(admin().jwt(jwt -> jwt.subject(adminId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"corrective_action\":\"Asked for silence in room 3\","
                                + "\"acknowledged_by\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledged_by").value(adminId.toString()))
                .andExpect(jsonPath("$.acknowledged_by_name").value("Ana Torres"));

        ArgumentCaptor<AcknowledgeAlertCommand> command = ArgumentCaptor.forClass(AcknowledgeAlertCommand.class);
        verify(acknowledgeAlert).execute(command.capture());
        assertThat(command.getValue().userId()).isEqualTo(adminId);
        assertThat(command.getValue().alertId()).isEqualTo(alertId);
    }

    @Test
    @DisplayName("409: an alert already acknowledged or closed")
    void notOpen() throws Exception {
        when(acknowledgeAlert.execute(any()))
                .thenThrow(AlertingError.ALERT_NOT_OPEN.with(alertId, AlertStatus.CLOSED));

        mockMvc.perform(patch("/api/v1/alerts/" + alertId + "/acknowledge")
                        .with(admin().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON).content(ACK))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALERTING_ALERT_NOT_OPEN"));
    }

    @Test
    @DisplayName("400: the corrective action is required, 3 to 500 characters")
    void actionRequired() throws Exception {
        for (String body : List.of("{}", "{\"corrective_action\":\"ok\"}",
                "{\"corrective_action\":\"" + "x".repeat(501) + "\"}")) {
            mockMvc.perform(patch("/api/v1/alerts/" + alertId + "/acknowledge")
                            .with(admin().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].code").value("ALERTING_CORRECTIVE_ACTION_REQUIRED"));
        }
        verifyNoInteractions(acknowledgeAlert);
    }

    private Alert openAlert() {
        return Alert.builder().id(alertId).sourceKey("edge-lima-centro:42").roomId(roomId)
                .rule("sustained_noise").severity(AlertSeverity.WARNING)
                .message("Noise above 60 dBA for 2 min").value(66.4).status(AlertStatus.OPEN)
                .openedAt(OffsetDateTime.parse("2026-11-08T14:10:00Z")).build();
    }
}
