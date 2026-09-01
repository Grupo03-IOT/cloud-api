package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.CreateApiCredentialUseCase;
import com.pe.cloudapi.iam.application.internal.results.CreatedCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.iam.interfaces.rest.resources.CreateCredentialResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.CredentialResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Machine credentials")
public class CredentialsController {

    private final CreateApiCredentialUseCase createApiCredential;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Issue a credential for a machine",
            description = """
                    The key is returned in clear text ONCE and never again: only \
                    its hash is stored. Copy it now; if it is lost, issue another \
                    one. Revoking is immediate — the key travels on every request, \
                    so there is no token left over to outlive it.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credential issued"),
            @ApiResponse(responseCode = "400", description = "Unknown scope"),
            @ApiResponse(responseCode = "409", description = "The code is already used")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public CredentialResource create(@Valid @RequestBody CreateCredentialResource resource) {
        Set<Scope> scopes = resource.scopes().stream().map(Scope::fromCode)
                .collect(Collectors.toSet());

        CreatedCredential created = createApiCredential.execute(resource.code(), scopes);
        return new CredentialResource(
                created.credential().getId(),
                created.credential().getCode(),
                created.credential().isActive(),
                created.credential().getScopes().stream().map(Scope::toCode).sorted().toList(),
                created.apiKey());
    }
}
