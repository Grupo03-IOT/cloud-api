package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.monitoring.application.internal.ports.in.IngestReadingsUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.IngestResult;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.ReadingResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReadingsController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({ReadingsController.class, ReadingResourceAssembler.class})
class ReadingsControllerTest extends WebLayerTestSupport {
    @MockitoBean IngestReadingsUseCase ingest;

    @Test
    void acceptsPhysicalSensorsFromTheUsbPipelineAndRejectsInvalidAdc() throws Exception {
        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-1", "hash", Set.of(Scope.READINGS_WRITE))));
        when(ingest.execute(any())).thenReturn(new IngestResult(1, 1, 0));
        String body = """
                {"site_code":"site-1","readings":[{"room_id":"sala-01",
                "ts":"2026-11-11T12:00:00Z","period_s":60,
                "climate":{"temp_c":28.6,"rh_pct":75.6},
                "sensors":{"sampled_at":"2026-11-11T12:00:58Z","movement":true,
                "distance_cm":154.14,"potentiometer":873,
                "button1":false,"button2":true,"button3":false}}]}
                """;
        mockMvc.perform(post("/api/v1/readings").header("X-API-Key", "key")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isAccepted());
        var command = ArgumentCaptor.forClass(IngestReadingsCommand.class);
        verify(ingest).execute(command.capture());
        var sensors = command.getValue().readings().getFirst().sensors();
        assertEquals(873, sensors.potentiometer());
        assertEquals(true, sensors.button2());
        mockMvc.perform(post("/api/v1/readings").header("X-API-Key", "key")
                .contentType(MediaType.APPLICATION_JSON).content(body.replace(":873", ":4096")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passesTheSiteCodeFromTheEdgeEnvelope() throws Exception {
        when(authenticateApiKey.execute(anyString())).thenReturn(Optional.of(
                new ApiCredential("edge-1", "hash", Set.of(Scope.READINGS_WRITE))));
        when(ingest.execute(any())).thenReturn(new IngestResult(1, 1, 0));
        mockMvc.perform(post("/api/v1/readings").header("X-API-Key", "key")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"site_code":"site-2","readings":[{"room_id":"sala-01",
                    "ts":"2026-01-01T12:00:00Z","period_s":60}]}
                    """)).andExpect(status().isAccepted());
        var command = ArgumentCaptor.forClass(IngestReadingsCommand.class);
        verify(ingest).execute(command.capture());
        assertEquals("site-2", command.getValue().siteCode());
        assertEquals("sala-01", command.getValue().readings().getFirst().roomCode());
    }
}
