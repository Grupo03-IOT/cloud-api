package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials")
public record SignInResource(@NotBlank String email, @NotBlank String password) {}
