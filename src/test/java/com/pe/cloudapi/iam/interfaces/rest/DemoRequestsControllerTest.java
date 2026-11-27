package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.ListDemoRequestsUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RequestDemoUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import com.pe.cloudapi.iam.interfaces.rest.transform.DemoRequestResourceAssembler;
import com.pe.cloudapi.shared.interfaces.rest.CorsConfiguration;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DemoRequestsController.class,
        properties = "app.cors.allowed-origins=https://app.example")
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({DemoRequestsController.class, DemoRequestResourceAssembler.class,
        DemoRequestRateLimitInterceptor.class, DemoRequestWebConfiguration.class,
        CorsConfiguration.class})
@DisplayName("/api/v1/demo-requests")
class DemoRequestsControllerTest extends WebLayerTestSupport {

    private static final String URL = "/api/v1/demo-requests";
    private static final String LANDING = "https://grupo03-iot.github.io";
    private static final String APP = "https://app.example";
    private static final String BODY = "{\"full_name\":\"Ana Torres\",\"work_email\":\"ana@coworking.pe\","
            + "\"company\":\"Coworking Lima\",\"room_count\":5,\"consent\":true,\"website\":\"\"}";

    @MockitoBean private RequestDemoUseCase requestDemo;
    @MockitoBean private ListDemoRequestsUseCase listRequests;

    @Test
    @DisplayName("201: a visitor without credentials; the receipt does not echo personal data")
    void created() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestDemo.execute(any())).thenReturn(stored(id));

        mockMvc.perform(send("203.0.113.1", BODY).header(HttpHeaders.ORIGIN, LANDING))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LANDING))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.created_at").exists())
                .andExpect(jsonPath("$.work_email").doesNotExist());

        verify(requestDemo).execute(
                new RequestDemoCommand("Ana Torres", "ana@coworking.pe", "Coworking Lima", 5));
    }

    @Test
    @DisplayName("201: a filled honeypot is answered like a success and stored nowhere")
    void honeypot() throws Exception {
        mockMvc.perform(send("203.0.113.2", BODY.replace("\"website\":\"\"", "\"website\":\"http://spam\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        verifyNoInteractions(requestDemo);
    }

    @Test
    @DisplayName("400: empty fields, bad email, rooms out of range or no consent")
    void invalid() throws Exception {
        String[][] cases = {
                {BODY.replace("Ana Torres", " "), "full_name"},
                {BODY.replace("Ana Torres", "x".repeat(101)), "full_name"},
                {BODY.replace("ana@coworking.pe", "ana@coworking"), "work_email"},
                {BODY.replace("ana@coworking.pe", "not-an-email"), "work_email"},
                {BODY.replace("Coworking Lima", ""), "company"},
                {BODY.replace("\"room_count\":5", "\"room_count\":0"), "room_count"},
                {BODY.replace("\"room_count\":5", "\"room_count\":10001"), "room_count"},
                {BODY.replace("\"room_count\":5,", ""), "room_count"},
                {BODY.replace("\"consent\":true", "\"consent\":false"), "consent"},
                {BODY.replace("\"consent\":true,", ""), "consent"}};

        for (int i = 0; i < cases.length; i++) {
            mockMvc.perform(send("198.51.100." + i, cases[i][0]))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("API_VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.errors[*].field", hasItem(cases[i][1])));
        }
        verifyNoInteractions(requestDemo);
    }

    @Test
    @DisplayName("429: the sixth request from one address, readable by the landing")
    void tooMany() throws Exception {
        when(requestDemo.execute(any())).thenReturn(stored(UUID.randomUUID()));
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(send("203.0.113.3", BODY)).andExpect(status().isCreated());
        }

        mockMvc.perform(send("203.0.113.3", BODY).header(HttpHeaders.ORIGIN, LANDING))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LANDING))
                .andExpect(jsonPath("$.code").value("IAM_DEMO_REQUEST_LIMIT_REACHED"));
        mockMvc.perform(send("203.0.113.4", BODY)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("200: an ADMIN reads the requests")
    void adminLists() throws Exception {
        when(listRequests.execute()).thenReturn(List.of(stored(UUID.randomUUID())));

        mockMvc.perform(get(URL).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].work_email").value("ana@coworking.pe"))
                .andExpect(jsonPath("$[0].room_count").value(5))
                .andExpect(jsonPath("$[0].consented_at").exists());
    }

    @Test
    @DisplayName("401 without a credential and 403 for a MEMBER")
    void listIsProtected() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(URL).with(member())).andExpect(status().isForbidden());
        verifyNoInteractions(listRequests);
    }

    @Test
    @DisplayName("preflight: the landing and the app may send the form")
    void preflight() throws Exception {
        for (String origin : List.of(LANDING, APP)) {
            mockMvc.perform(preflightOf(URL, origin, "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }
        mockMvc.perform(preflightOf(URL, APP, "GET")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("preflight: the landing gets nothing else, and unknown origins get nothing")
    void preflightRejected() throws Exception {
        for (MockHttpServletRequestBuilder request : List.of(
                preflightOf(URL, LANDING, "GET"),
                preflightOf("/api/v1/users", LANDING, "POST"),
                preflightOf("/api/v1/auth/login", LANDING, "POST"),
                preflightOf(URL, "https://evil.example", "POST"))) {
            mockMvc.perform(request)
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    /** The limit is kept per address, so each test calls from its own. */
    private static MockHttpServletRequestBuilder send(String ip, String body) {
        return post(URL).header("X-Forwarded-For", ip)
                .contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder preflightOf(String url, String origin, String method) {
        return options(url)
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type");
    }

    private static DemoRequest stored(UUID id) {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-27T15:00:00Z");
        return new DemoRequest(id, "Ana Torres", "ana@coworking.pe", "Coworking Lima", 5, now, now);
    }
}
