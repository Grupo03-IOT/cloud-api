package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.ListUsersUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RegisterUserUseCase;
import com.pe.cloudapi.iam.interfaces.rest.resources.RegisterUserResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.UserResource;
import com.pe.cloudapi.iam.interfaces.rest.transform.UserResourceAssembler;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Accounts")
public class UsersController {

    private final RegisterUserUseCase registerUser;
    private final ListUsersUseCase listUsers;
    private final UserResourceAssembler assembler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register an account",
            description = "Always created as MEMBER. Granting ADMIN is an administration task.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "409", description = "The email is already used")
    })
    public UserResource register(@Valid @RequestBody RegisterUserResource resource) {
        return assembler.toResource(registerUser.execute(assembler.toCommand(resource)));
    }

    @GetMapping
    @Operation(summary = "List the accounts with their roles", description = "Sorted by email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts returned"),
            @ApiResponse(responseCode = "403", description = "Only an administrator can list accounts")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResource> list() {
        return listUsers.execute().stream().map(assembler::toResource).toList();
    }
}
