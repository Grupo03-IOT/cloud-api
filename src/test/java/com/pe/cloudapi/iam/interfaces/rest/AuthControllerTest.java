package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateUserUseCase;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import(AuthController.class)
@DisplayName("TS7 - POST /api/v1/auth/login (sign in)")
class AuthControllerTest extends WebLayerTestSupport {

    @MockitoBean
    private AuthenticateUserUseCase authenticateUser;

    @Test
    @DisplayName("200: returns a Bearer token with its lifetime for valid credentials, without a prior token")
    void signInReturnsBearerToken() throws Exception {
        when(authenticateUser.execute("ana@comfort.pe", "s3cretPass"))
                .thenReturn(new IssuedToken("eyJhbGciOiJSUzI1NiJ9.payload.signature", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@comfort.pe","password":"s3cretPass"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("eyJhbGciOiJSUzI1NiJ9.payload.signature"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    @DisplayName("401: an unknown email and a wrong password give the same generic error")
    void wrongCredentialsAre401() throws Exception {
        when(authenticateUser.execute(eq("ghost@comfort.pe"), anyString()))
                .thenThrow(IamError.INVALID_CREDENTIALS.with());
        when(authenticateUser.execute(eq("ana@comfort.pe"), eq("wrong")))
                .thenThrow(IamError.INVALID_CREDENTIALS.with());

        for (String body : new String[]{
                "{\"email\":\"ghost@comfort.pe\",\"password\":\"whatever\"}",
                "{\"email\":\"ana@comfort.pe\",\"password\":\"wrong\"}"}) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("IAM_INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").value("Email or password is not correct"));
        }
    }

    @Test
    @DisplayName("401: a deactivated account is refused")
    void disabledAccountIs401() throws Exception {
        when(authenticateUser.execute("ana@comfort.pe", "s3cretPass"))
                .thenThrow(IamError.ACCOUNT_DISABLED.with());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@comfort.pe\",\"password\":\"s3cretPass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IAM_ACCOUNT_DISABLED"));
    }

    @Test
    @DisplayName("400: only a malformed body is a bad request")
    void malformedBodyIs400() throws Exception {
        for (String body : new String[]{"{\"email\":\"ana@comfort.pe\"}", "{\"email\":", "{}"}) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(authenticateUser);
    }
}
