package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "New account")
public record RegisterUserResource(

        @NotBlank @Email @Size(max = 160)
        String email,

        @NotBlank @Size(min = 8, max = 72)
        @Schema(description = "At least 8 characters. BCrypt ignores anything past 72")
        String password,

        @Size(max = 128)
        String displayName
) {}
