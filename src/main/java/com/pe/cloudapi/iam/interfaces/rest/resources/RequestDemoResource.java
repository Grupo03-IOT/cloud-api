package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Lo que envía el formulario del Landing Page. */
@Schema(description = "Demo request sent by a visitor")
public record RequestDemoResource(

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Ana Torres")
        String fullName,

        @NotBlank
        @Size(max = 150)
        @Email(regexp = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", message = "IAM_DEMO_EMAIL_INVALID")
        @Schema(example = "ana@coworking.pe")
        String workEmail,

        @NotBlank
        @Size(max = 150)
        @Schema(description = "Coworking or organization", example = "Coworking Lima")
        String company,

        @NotNull
        @Positive
        @Max(value = 10000, message = "IAM_DEMO_ROOM_COUNT_TOO_HIGH")
        @Schema(description = "Number of rooms, 1 to 10000", example = "5")
        Integer roomCount,

        @NotNull(message = "IAM_DEMO_CONSENT_REQUIRED")
        @AssertTrue(message = "IAM_DEMO_CONSENT_REQUIRED")
        @Schema(description = "Must be true: the visitor agrees to the data being stored to answer the request",
                example = "true")
        Boolean consent,

        @Schema(description = "Anti-spam field. Leave it empty: a request that fills it is discarded")
        String website
) {

    public boolean isHoneypotFilled() {
        return website != null && !website.isBlank();
    }
}
