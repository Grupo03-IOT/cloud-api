package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateUserUseCase;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;
import com.pe.cloudapi.iam.interfaces.rest.resources.SignInResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.TokenResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Sign in and tokens")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sign in with email and password",
            description = """
                    Returns a token to send as 'Authorization: Bearer <token>'. \
                    A wrong password and an unknown email give the same error on \
                    purpose: telling them apart would turn this into a way to \
                    find out which accounts exist.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signed in"),
            @ApiResponse(responseCode = "400", description = "Credentials are not correct")
    })
    public TokenResource login(@Valid @RequestBody SignInResource resource) {
        IssuedToken token = authenticateUser.execute(resource.email(), resource.password());
        return new TokenResource(token.value(), "Bearer", token.expiresInSeconds());
    }
}
