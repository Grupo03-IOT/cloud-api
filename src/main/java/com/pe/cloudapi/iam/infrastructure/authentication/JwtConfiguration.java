package com.pe.cloudapi.iam.infrastructure.authentication;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Con qué se firman y se validan los tokens.
 */
@Slf4j
@Configuration
public class JwtConfiguration {

    /**
     * <strong>Se genera al arrancar.</strong> Un reinicio invalida los tokens
     * vivos, y con más de una réplica cada una firma distinto: antes de
     * desplegar replicado hay que leerla de la configuración.
     */
    @Bean
    public RSAKey signingKey() {
        log.warn("Clave de firma generada al arrancar: no vale para más de una instancia");
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
}
