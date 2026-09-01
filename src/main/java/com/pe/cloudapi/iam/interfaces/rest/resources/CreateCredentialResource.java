package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "New machine credential")
public record CreateCredentialResource(

        @NotBlank @Size(max = 64)
        @Schema(description = "Stable name of the machine", example = "edge-lima-centro")
        String code,

        @NotEmpty
        @Schema(description = "What it may do. One of: readings:write, thresholds:read",
                example = "[\"readings:write\",\"thresholds:read\"]")
        Set<String> scopes
) {}
