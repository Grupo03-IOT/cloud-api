package com.pe.cloudapi.iam.infrastructure.authentication;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Token signing key")
class JwtConfigurationTest {

    @Test
    @DisplayName("is read from base64 PKCS#8 and keeps the same id on every start")
    void readsBase64Pkcs8() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        String encoded = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

        RSAKey first = JwtConfiguration.fromBase64(encoded);
        RSAKey second = JwtConfiguration.fromBase64(encoded);

        assertThat(first.toRSAPublicKey().getModulus())
                .isEqualTo(((RSAPublicKey) pair.getPublic()).getModulus());
        assertThat(first.isPrivate()).isTrue();
        assertThat(first.getKeyID()).isEqualTo(second.getKeyID());
    }

    @Test
    @DisplayName("refuses a value that is not a key, so the application does not start with a random one")
    void rejectsGarbage() {
        assertThatThrownBy(() -> JwtConfiguration.fromBase64("bm90IGEga2V5"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SIGNING_KEY");
    }

    @Test
    @DisplayName("refuses a value that is not base64")
    void rejectsNonBase64() {
        assertThatThrownBy(() -> JwtConfiguration.fromBase64("-----BEGIN PRIVATE KEY-----"))
                .isInstanceOf(IllegalStateException.class);
    }
}
