package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Una cuenta, tal como se devuelve. Sin la contraseña ni su hash: no hay ningún
 * caso en que un cliente necesite verlos.
 */
@Schema(description = "Account")
public record UserResource(UUID id, String email, String displayName,
                           Boolean active, List<String> roles) {}
