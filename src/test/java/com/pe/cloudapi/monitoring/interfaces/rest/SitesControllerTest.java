package com.pe.cloudapi.monitoring.interfaces.rest;

import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateRoomTypeUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateSiteUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomTypesUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSitesUseCase;
import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.RoomTypeResourceAssembler;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.SiteResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SitesController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({SitesController.class, SiteResourceAssembler.class, RoomTypeResourceAssembler.class})
@DisplayName("TS8 - POST /api/v1/sites (register a site)")
class SitesControllerTest extends WebLayerTestSupport {

    private static final String SITE_JSON = """
            {"code":"coworking-lima-centro","name":"Coworking Lima Centro","address":"Av. Arequipa 123"}""";

    @MockitoBean private CreateSiteUseCase createSite;
    @MockitoBean private ListSitesUseCase listSites;
    @MockitoBean private CreateRoomTypeUseCase createRoomType;
    @MockitoBean private ListRoomTypesUseCase listRoomTypes;

    @Test
    @DisplayName("201: an administrator registers a site")
    void adminRegistersSite() throws Exception {
        UUID siteId = UUID.randomUUID();
        when(createSite.execute(any())).thenReturn(Site.builder()
                .id(siteId).code("coworking-lima-centro").name("Coworking Lima Centro")
                .address("Av. Arequipa 123").timezone("America/Lima").build());

        mockMvc.perform(post("/api/v1/sites").with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(SITE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(siteId.toString()))
                .andExpect(jsonPath("$.code").value("coworking-lima-centro"))
                .andExpect(jsonPath("$.timezone").value("America/Lima"));
    }

    @Test
    @DisplayName("403: a MEMBER cannot register a site")
    void memberCannotRegisterSite() throws Exception {
        mockMvc.perform(post("/api/v1/sites").with(member())
                        .contentType(MediaType.APPLICATION_JSON).content(SITE_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("API_FORBIDDEN"));

        verifyNoInteractions(createSite);
    }
}
