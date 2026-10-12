package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.CreateApiCredentialUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.ListApiCredentialsUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RevokeApiCredentialUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CredentialsController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import(CredentialsController.class)
@DisplayName("GET and DELETE /api/v1/credentials (list and revoke machine credentials)")
class CredentialsControllerTest extends WebLayerTestSupport {

    @MockitoBean private CreateApiCredentialUseCase createApiCredential;
    @MockitoBean private ListApiCredentialsUseCase listApiCredentials;
    @MockitoBean private RevokeApiCredentialUseCase revokeApiCredential;

    @Test
    @DisplayName("200: an administrator lists credentials, never with their key")
    void adminListsWithoutKey() throws Exception {
        when(listApiCredentials.execute()).thenReturn(List.of(ApiCredential.builder()
                .id(UUID.randomUUID()).code("edge-lima-centro").tokenHash("hash").active(false)
                .scopes(Set.of(Scope.READINGS_WRITE, Scope.THRESHOLDS_READ)).build()));

        mockMvc.perform(get("/api/v1/credentials").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("edge-lima-centro"))
                .andExpect(jsonPath("$[0].active").value(false))
                .andExpect(jsonPath("$[0].scopes[0]").value("readings:write"))
                .andExpect(jsonPath("$[0].api_key").doesNotExist());
    }

    @Test
    @DisplayName("204: an administrator revokes a credential")
    void adminRevokes() throws Exception {
        UUID credentialId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/credentials/{id}", credentialId).with(admin()))
                .andExpect(status().isNoContent());

        verify(revokeApiCredential).execute(credentialId);
    }

    @Test
    @DisplayName("404: revoking a credential that does not exist")
    void revokeUnknown() throws Exception {
        UUID credentialId = UUID.randomUUID();
        doThrow(IamError.CREDENTIAL_NOT_FOUND.with(credentialId))
                .when(revokeApiCredential).execute(credentialId);

        mockMvc.perform(delete("/api/v1/credentials/{id}", credentialId).with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("IAM_CREDENTIAL_NOT_FOUND"));
    }

    @Test
    @DisplayName("403: a MEMBER cannot revoke a credential")
    void memberCannotRevoke() throws Exception {
        mockMvc.perform(delete("/api/v1/credentials/{id}", UUID.randomUUID()).with(member()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(revokeApiCredential);
    }
}
