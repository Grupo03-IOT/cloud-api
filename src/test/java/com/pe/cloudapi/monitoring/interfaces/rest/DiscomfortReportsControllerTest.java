package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.monitoring.application.internal.ports.in.FileDiscomfortReportUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListDiscomfortReportsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.errors.MonitoringError;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.DiscomfortReportResourceAssembler;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiscomfortReportsController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({DiscomfortReportsController.class, DiscomfortReportResourceAssembler.class})
@DisplayName("/api/v1/discomfort-reports")
class DiscomfortReportsControllerTest extends WebLayerTestSupport {

    @MockitoBean private FileDiscomfortReportUseCase fileReport;
    @MockitoBean private ListDiscomfortReportsUseCase listReports;

    private final UUID roomId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private String body() {
        return "{\"room_id\":\"" + roomId + "\",\"category\":\"WARM\","
                + "\"comment\":\"Hace calor cerca de la ventana\"}";
    }

    @Test
    @DisplayName("201: a MEMBER files a report; the user comes from the token, not the body")
    void memberFiles() throws Exception {
        when(fileReport.execute(any())).thenReturn(new RoomDiscomfortReport(report(), "sala-03"));

        mockMvc.perform(post("/api/v1/discomfort-reports")
                        .with(member().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body().replace("}", ",\"user_id\":\"" + UUID.randomUUID() + "\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.room_code").value("sala-03"))
                .andExpect(jsonPath("$.category").value("WARM"))
                .andExpect(jsonPath("$.reading.temp_c").value(24.9))
                .andExpect(jsonPath("$.reading.laeq").value(52.1));

        ArgumentCaptor<FileDiscomfortReportCommand> command =
                ArgumentCaptor.forClass(FileDiscomfortReportCommand.class);
        verify(fileReport).execute(command.capture());
        assertThat(command.getValue().userId()).isEqualTo(userId);
        assertThat(command.getValue().roomId()).isEqualTo(roomId);
        assertThat(command.getValue().category()).isEqualTo(DiscomfortCategory.WARM);
    }

    @Test
    @DisplayName("201: an ADMIN may file too, and reading is null when there was none")
    void adminFilesWithoutReading() throws Exception {
        when(fileReport.execute(any())).thenReturn(new RoomDiscomfortReport(
                report().toBuilder().reading(null).comment(null).build(), "sala-03"));

        mockMvc.perform(post("/api/v1/discomfort-reports")
                        .with(admin().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reading").isEmpty())
                .andExpect(jsonPath("$.comment").isEmpty());
    }

    @Test
    @DisplayName("409: filing the same category in the same room too often")
    void tooFrequent() throws Exception {
        when(fileReport.execute(any())).thenThrow(
                MonitoringError.DISCOMFORT_TOO_FREQUENT.with("WARM", "2026-11-08T15:00:00Z"));

        mockMvc.perform(post("/api/v1/discomfort-reports")
                        .with(member().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MONITORING_DISCOMFORT_TOO_FREQUENT"));
    }

    @Test
    @DisplayName("404: an unknown room")
    void unknownRoom() throws Exception {
        when(fileReport.execute(any())).thenThrow(MonitoringError.ROOM_NOT_FOUND.with(roomId));

        mockMvc.perform(post("/api/v1/discomfort-reports")
                        .with(member().jwt(jwt -> jwt.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MONITORING_ROOM_NOT_FOUND"));
    }

    @Test
    @DisplayName("400: missing room, unknown category or comment over 500 characters")
    void invalid() throws Exception {
        for (String bad : List.of(
                "{\"category\":\"WARM\"}",
                body().replace("WARM", "SMELL"),
                body().replace("Hace calor cerca de la ventana", "x".repeat(501)))) {
            mockMvc.perform(post("/api/v1/discomfort-reports")
                            .with(member().jwt(jwt -> jwt.subject(userId.toString())))
                            .contentType(MediaType.APPLICATION_JSON).content(bad))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(fileReport);
    }

    @Test
    @DisplayName("401: no credential, or an Edge key, cannot file a report")
    void unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/discomfort-reports")
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isUnauthorized());

        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-1", "hash", Set.of(Scope.READINGS_WRITE))));
        mockMvc.perform(post("/api/v1/discomfort-reports").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(body()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(fileReport);
    }

    @Test
    @DisplayName("200: an ADMIN lists with filters")
    void adminLists() throws Exception {
        when(listReports.execute(any())).thenReturn(List.of(new RoomDiscomfortReport(report(), "sala-03")));

        mockMvc.perform(get("/api/v1/discomfort-reports").with(admin())
                        .param("room_id", roomId.toString()).param("category", "warm")
                        .param("from", "2026-11-08T00:00:00Z").param("to", "2026-11-09T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(userId.toString()));

        ArgumentCaptor<ListDiscomfortReportsQuery> query = ArgumentCaptor.forClass(ListDiscomfortReportsQuery.class);
        verify(listReports).execute(query.capture());
        assertThat(query.getValue().category()).isEqualTo(DiscomfortCategory.WARM);
        assertThat(query.getValue().roomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("403: a MEMBER cannot list all reports nor read the summary")
    void memberForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/discomfort-reports").with(member())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/discomfort-reports/summary").with(member()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(listReports);
    }

    @Test
    @DisplayName("200: anyone signed in sees their own reports")
    void mine() throws Exception {
        when(listReports.ofUser(userId)).thenReturn(List.of(new RoomDiscomfortReport(report(), "sala-03")));

        mockMvc.perform(get("/api/v1/discomfort-reports/mine")
                        .with(member().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/discomfort-reports/mine")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("200: an ADMIN reads the summary; 400 on an inverted range")
    void summary() throws Exception {
        when(listReports.summarize(any(), any())).thenReturn(List.of(
                new DiscomfortSummary(roomId, "sala-03", 3, 5, 0)));

        mockMvc.perform(get("/api/v1/discomfort-reports/summary").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].room_code").value("sala-03"))
                .andExpect(jsonPath("$[0].noise").value(3))
                .andExpect(jsonPath("$[0].warm").value(5))
                .andExpect(jsonPath("$[0].cold").value(0))
                .andExpect(jsonPath("$[0].total").value(8));

        mockMvc.perform(get("/api/v1/discomfort-reports/summary").with(admin())
                        .param("from", "2026-11-09T00:00:00Z").param("to", "2026-11-08T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MONITORING_RANGE_INVERTED"));
    }

    private DiscomfortReport report() {
        return DiscomfortReport.builder().id(UUID.randomUUID()).roomId(roomId).userId(userId)
                .category(DiscomfortCategory.WARM).comment("Hace calor cerca de la ventana")
                .createdAt(OffsetDateTime.parse("2026-11-08T15:00:00Z"))
                .reading(new ReadingAtReport(OffsetDateTime.parse("2026-11-08T14:59:00Z"),
                        52.1f, 24.9f, 58f, 100f))
                .build();
    }
}
