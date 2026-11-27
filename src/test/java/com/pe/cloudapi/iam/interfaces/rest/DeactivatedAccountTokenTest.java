package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.ListUsersUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RegisterUserUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.SetUserActiveUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.infrastructure.authentication.JwtTokenIssuer;
import com.pe.cloudapi.iam.interfaces.rest.transform.UserResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * US10: a token issued before the account was deactivated stops working.
 *
 * <p>Unlike the other web tests, these send a real signed JWT, so the request
 * goes through the production decoder and {@code ActiveAccountJwtConverter}.
 */
@WebMvcTest(controllers = UsersController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({UsersController.class, UserResourceAssembler.class, JwtTokenIssuer.class})
@DisplayName("US10 - requests with the token of a deactivated account")
class DeactivatedAccountTokenTest extends WebLayerTestSupport {

    @MockitoBean private RegisterUserUseCase registerUser;
    @MockitoBean private ListUsersUseCase listUsers;
    @MockitoBean private SetUserActiveUseCase setUserActive;

    @Autowired private JwtTokenIssuer issuer;

    private final User admin = User.builder().id(UUID.randomUUID()).email("admin@comfort.pe")
            .passwordHash("hash").displayName("Admin").active(true).roles(Set.of(Role.ADMIN)).build();

    @Test
    @DisplayName("200: the token of an active account works")
    void activeAccount() throws Exception {
        when(isUserActive.execute(admin.getId())).thenReturn(true);
        when(listUsers.execute()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("401 IAM_ACCOUNT_DISABLED: the same token once the account is deactivated")
    void deactivatedAccount() throws Exception {
        when(isUserActive.execute(admin.getId())).thenReturn(false);

        mockMvc.perform(get("/api/v1/users").header("Authorization", bearer()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IAM_ACCOUNT_DISABLED"));

        verifyNoInteractions(listUsers);
    }

    @Test
    @DisplayName("401 UNAUTHORIZED: a token that is not ours keeps the generic code")
    void foreignToken() throws Exception {
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("API_UNAUTHORIZED"));
    }

    private String bearer() {
        return "Bearer " + issuer.issueFor(admin).value();
    }
}
