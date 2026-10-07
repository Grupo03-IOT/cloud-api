package com.pe.cloudapi.support;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateApiKeyUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Base for the {@code @WebMvcTest} controller tests: MockMvc plus the API key
 * use case the production security filter depends on.
 */
public abstract class WebLayerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthenticateApiKeyUseCase authenticateApiKey;

    protected static JwtRequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    protected static JwtRequestPostProcessor member() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }
}
