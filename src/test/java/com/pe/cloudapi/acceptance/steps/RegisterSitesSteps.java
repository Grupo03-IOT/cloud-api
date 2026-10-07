package com.pe.cloudapi.acceptance.steps;

import com.pe.cloudapi.monitoring.domain.model.aggregates.Site;
import com.pe.cloudapi.monitoring.domain.ports.out.SiteRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Step definitions for US11 - Register sites. */
public class RegisterSitesSteps {

    @Autowired private MockMvc mockMvc;
    @Autowired private SiteRepository siteRepository;

    private JwtRequestPostProcessor user;
    private MvcResult result;

    @Before
    public void resetRepository() {
        reset(siteRepository);
    }

    @Given("no site with code {string} is registered")
    public void noSiteIsRegistered(String code) {
        when(siteRepository.findByCode(code)).thenReturn(Optional.empty());
        when(siteRepository.save(any(Site.class)))
                .thenAnswer(inv -> ((Site) inv.getArgument(0)).toBuilder().id(UUID.randomUUID()).build());
    }

    @Given("the site {string} is already registered")
    public void theSiteIsAlreadyRegistered(String code) {
        when(siteRepository.findByCode(code)).thenReturn(Optional.of(
                Site.builder().id(UUID.randomUUID()).code(code).name("Existing site").build()));
    }

    @Given("I am signed in as {string}")
    public void iAmSignedInAs(String role) {
        user = jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @When("I register the site {string} named {string} without a timezone")
    public void iRegisterTheSite(String code, String name) throws Exception {
        result = mockMvc.perform(post("/api/v1/sites").with(user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"%s"}""".formatted(code, name)))
                .andReturn();
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(result.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the registered site has code {string} and timezone {string}")
    public void theRegisteredSiteHas(String code, String timezone) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertThat((String) read(body, "$.id")).isNotBlank();
        assertThat((String) read(body, "$.code")).isEqualTo(code);
        assertThat((String) read(body, "$.timezone")).isEqualTo(timezone);
    }

    @Then("the error code is {string}")
    public void theErrorCodeIs(String code) throws Exception {
        assertThat((String) read(result.getResponse().getContentAsString(), "$.code")).isEqualTo(code);
    }

    @Then("no site is stored")
    public void noSiteIsStored() {
        verify(siteRepository, never()).save(any());
    }
}
