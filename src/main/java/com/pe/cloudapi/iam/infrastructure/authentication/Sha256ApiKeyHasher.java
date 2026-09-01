package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.domain.ports.out.ApiKeyHasher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class Sha256ApiKeyHasher implements ApiKeyHasher {

    private static final int KEY_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    /**
     * Identifica a quien <strong>emite</strong> la clave y en qué entorno. Los
     * escáneres de secretos detectan este patrón en repositorios públicos.
     */
    private final String prefix;

    public Sha256ApiKeyHasher(@Value("${app.iam.api-key-prefix:comfort_live_}") String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String generate() {
        byte[] bytes = new byte[KEY_BYTES];
        random.nextBytes(bytes);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
