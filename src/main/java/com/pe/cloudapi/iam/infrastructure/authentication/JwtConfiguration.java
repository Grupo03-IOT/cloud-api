package com.pe.cloudapi.iam.infrastructure.authentication;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * Con qué se firman y se validan los tokens.
 */
@Slf4j
@Configuration
public class JwtConfiguration {

    /**
     * La clave RSA de {@code app.iam.signing-key}: el PKCS#8 en base64, en una
     * sola línea, sin las líneas {@code BEGIN}/{@code END} del PEM.
     *
     * <p>Sin ella se genera al arrancar: un reinicio invalida los tokens vivos
     * y cada réplica firmaría distinto. Vale para desarrollo, no para producción.
     *
     * @throws IllegalStateException si viene y no es una clave RSA: la
     *         aplicación no arranca
     */
    @Bean
    public RSAKey signingKey(@Value("${app.iam.signing-key:}") String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            log.warn("Clave de firma generada al arrancar: no vale para más de una instancia");
            return generated();
        }
        return fromBase64(encodedKey);
    }

    /** Firma con la privada. */
    @Bean
    public JwtEncoder jwtEncoder(RSAKey signingKey) {
        JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(signingKey));
        return new NimbusJwtEncoder(source);
    }

    /** Valida con la pública. */
    @Bean
    public JwtDecoder jwtDecoder(RSAKey signingKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(signingKey.toRSAPublicKey()).build();
    }

    static RSAKey fromBase64(String encodedKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) factory.generatePrivate(
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedKey.replaceAll("\\s", ""))));
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                    new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyIDFromThumbprint()
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "JWT_SIGNING_KEY no es una clave RSA en PKCS#8 codificada en base64", ex);
        }
    }

    private static RSAKey generated() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                    .privateKey((RSAPrivateKey) pair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar la clave de firma", ex);
        }
    }
}
