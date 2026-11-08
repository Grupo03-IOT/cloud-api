package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de una cuenta")
public record UpdateUserStatusResource(
        @Schema(description = "false desactiva la cuenta; true la reactiva") @NotNull Boolean active
) {}
