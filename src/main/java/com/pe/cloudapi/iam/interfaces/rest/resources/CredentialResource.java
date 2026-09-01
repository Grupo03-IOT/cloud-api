package com.pe.cloudapi.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * @param apiKey en claro y <strong>solo al crearla</strong>. Después no vuelve a
 *               aparecer aquí ni en ningún otro sitio: de ella solo queda el
 *               hash. Si se pierde, se emite otra
 */
@Schema(description = "Machine credential")
public record CredentialResource(UUID id, String code, Boolean active,
                                 List<String> scopes, String apiKey) {}
