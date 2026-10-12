package com.pe.cloudapi.insights.interfaces.rest;

import com.pe.cloudapi.insights.application.internal.ports.in.AnalyzeRoomUseCase;
import com.pe.cloudapi.insights.application.internal.ports.in.ProfileRoomHourlyUseCase;
import com.pe.cloudapi.insights.application.internal.ports.in.RankSiteRoomsUseCase;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.domain.services.ComfortAnalyticsService;
import com.pe.cloudapi.insights.interfaces.rest.transform.RoomAnalyticsResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RoomAnalyticsController.class, SiteRoomsRankingController.class})
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({RoomAnalyticsController.class, SiteRoomsRankingController.class, RoomAnalyticsResourceAssembler.class})
@DisplayName("GET /api/v1/insights (room analysis, hourly profile and site ranking)")
class RoomAnalyticsControllerTest extends WebLayerTestSupport {

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID SITE_ID = UUID.randomUUID();

    private final ComfortAnalyticsService analytics = new ComfortAnalyticsService();

    @MockitoBean private AnalyzeRoomUseCase analyzeRoom;
    @MockitoBean private ProfileRoomHourlyUseCase profileRoomHourly;
    @MockitoBean private RankSiteRoomsUseCase rankSiteRooms;

    @Test
    @DisplayName("200: a period without readings answers with no data, not with an error")
    void emptyPeriodIsNotAnError() throws Exception {
        when(analyzeRoom.execute(any())).thenReturn(analytics.analyze(ROOM_ID,
                Period.parse("2026-10").in(ZoneId.of("America/Lima")), List.of(), List.of(), 55f));

        mockMvc.perform(get("/api/v1/insights/rooms/{roomId}", ROOM_ID).param("period", "2026-10").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("2026-10"))
                .andExpect(jsonPath("$.timezone").value("America/Lima"))
                .andExpect(jsonPath("$.data_quality.coverage_pct").value(0.0))
                .andExpect(jsonPath("$.findings").isEmpty())
                .andExpect(jsonPath("$.measures.thermal_drift.verdict").value("no_data"))
                .andExpect(jsonPath("$.measures.thermal_drift.value").doesNotExist())
                .andExpect(jsonPath("$.noise_distribution").doesNotExist());
    }

    @Test
    @DisplayName("400: a malformed period is rejected with its own code")
    void malformedPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/insights/rooms/{roomId}", ROOM_ID).param("period", "2026-13").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSIGHTS_PERIOD_INVALID"));

        verifyNoInteractions(analyzeRoom);
    }

    @Test
    @DisplayName("400: the period is required")
    void missingPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/insights/rooms/{roomId}", ROOM_ID).with(admin()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analyzeRoom);
    }

    @Test
    @DisplayName("200: the hourly profile always has 24 hours")
    void hourlyProfile() throws Exception {
        when(profileRoomHourly.execute(any())).thenReturn(analytics.hourlyProfile(ROOM_ID,
                Period.parse("2026-10-12").in(ZoneId.of("America/Lima")), List.of()));

        mockMvc.perform(get("/api/v1/insights/rooms/{roomId}/hourly-profile", ROOM_ID)
                        .param("period", "2026-10-12").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours.length()").value(24))
                .andExpect(jsonPath("$.hours[0].verdict").value("no_data"));
    }

    @Test
    @DisplayName("403: a MEMBER cannot see the site ranking")
    void memberCannotRank() throws Exception {
        mockMvc.perform(get("/api/v1/insights/sites/{siteId}/rooms", SITE_ID)
                        .param("period", "2026-10").with(member()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("API_FORBIDDEN"));

        verifyNoInteractions(rankSiteRooms);
    }

    @Test
    @DisplayName("401: insights require a signed-in user")
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/insights/rooms/{roomId}", ROOM_ID).param("period", "2026-10"))
                .andExpect(status().isUnauthorized());
    }
}
