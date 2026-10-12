package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListGatewaysUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.RecordHeartbeatUseCase;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.GatewayResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EdgesController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({EdgesController.class, GatewayResourceAssembler.class})
@DisplayName("POST /api/v1/edges/heartbeat (Edge liveness)")
class EdgesControllerTest extends WebLayerTestSupport {

    private static final String HEARTBEAT_JSON = """
            {"code":"edge-lima-centro","site_code":"coworking-lima-centro","version":"0.1.0",\
            "pending_upload":0,"rooms":3}""";

    @MockitoBean private RecordHeartbeatUseCase recordHeartbeat;
    @MockitoBean private ListGatewaysUseCase listGateways;

    @Test
    @DisplayName("204: an Edge with the heartbeat scope reports it is alive")
    void edgeSendsHeartbeat() throws Exception {
        givenApiKeyWith(Scope.EDGES_HEARTBEAT);

        mockMvc.perform(post("/api/v1/edges/heartbeat").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(HEARTBEAT_JSON))
                .andExpect(status().isNoContent());

        verify(recordHeartbeat).execute("edge-lima-centro", "coworking-lima-centro", "0.1.0", 0, 3);
    }

    @Test
    @DisplayName("403: a credential that may only upload readings cannot send heartbeats")
    void wrongScope() throws Exception {
        givenApiKeyWith(Scope.READINGS_WRITE);

        mockMvc.perform(post("/api/v1/edges/heartbeat").header("X-API-Key", "key")
                        .contentType(MediaType.APPLICATION_JSON).content(HEARTBEAT_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recordHeartbeat);
    }

    private void givenApiKeyWith(Scope scope) {
        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-lima-centro", "hash", Set.of(scope))));
    }
}
