package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @param expiresIn segundos. Va para que el cliente renueve antes de caducar en
 *                  vez de descubrirlo con un 401
 */
@Schema(description = "Issued token")
public record TokenResource(String accessToken, String tokenType, Long expiresIn) {}
