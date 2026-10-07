package com.pe.cloudapi.acceptance;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateApiKeyUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.CreateRoomTypeUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListRoomTypesUseCase;
import com.pe.cloudapi.monitoring.application.internal.ports.in.ListSitesUseCase;
import com.pe.cloudapi.monitoring.application.internal.usecases.CreateSiteUseCaseImpl;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import com.pe.cloudapi.monitoring.interfaces.rest.SitesController;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.RoomTypeResourceAssembler;
import com.pe.cloudapi.monitoring.interfaces.rest.transform.SiteResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Spring context for the acceptance scenarios: the real controller, security
 * chain and site registration use case; only the site repository is mocked,
 * so no database is needed.
 */
@CucumberContextConfiguration
@WebMvcTest(controllers = SitesController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({SitesController.class, SiteResourceAssembler.class, RoomTypeResourceAssembler.class,
        CreateSiteUseCaseImpl.class})
public class CucumberSpringConfiguration {

    @MockitoBean private SiteRepository siteRepository;
    @MockitoBean private ListSitesUseCase listSites;
    @MockitoBean private CreateRoomTypeUseCase createRoomType;
    @MockitoBean private ListRoomTypesUseCase listRoomTypes;
    @MockitoBean private AuthenticateApiKeyUseCase authenticateApiKey;
}
