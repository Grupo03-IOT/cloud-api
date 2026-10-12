package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.CreateApiCredentialUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.ListApiCredentialsUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RevokeApiCredentialUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Credentials", description = "Machine credentials")
public class CredentialsController {

    private final CreateApiCredentialUseCase createApiCredential;
    private final ListApiCredentialsUseCase listApiCredentials;
    private final RevokeApiCredentialUseCase revokeApiCredential;

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
        return toResource(created.credential(), created.apiKey());
    }

    @GetMapping
    @Operation(summary = "List machine credentials",
            description = "Revoked ones included. The key is never returned again.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Credentials returned"))
    @PreAuthorize("hasRole('ADMIN')")
    public List<CredentialResource> list() {
        return listApiCredentials.execute().stream()
                .map(credential -> toResource(credential, null))
                .toList();
    }

    @DeleteMapping("/{credentialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke a machine credential",
            description = """
                    The machine gets 401 from its next request on. The credential is                     deactivated, not erased, and revoking it twice is not an error.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Credential revoked"),
            @ApiResponse(responseCode = "404", description = "Credential does not exist")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public void revoke(@PathVariable UUID credentialId) {
        revokeApiCredential.execute(credentialId);
    }

    private static CredentialResource toResource(ApiCredential credential, String apiKey) {
        return new CredentialResource(
                credential.getId(),
                credential.getCode(),
                credential.isActive(),
                credential.getScopes().stream().map(Scope::toCode).sorted().toList(),
                apiKey);
    }
}
