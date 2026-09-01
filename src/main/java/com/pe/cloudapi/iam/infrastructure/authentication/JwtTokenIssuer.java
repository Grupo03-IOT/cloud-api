package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;
import com.pe.cloudapi.iam.domain.ports.out.TokenIssuer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Emite el token como JWT.
 *
 * <p>Un JWT se firma pero no se cifra: <strong>lo que se meta aquí lo lee
 * cualquiera</strong> que tenga el token.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder encoder;

    @Value("${app.iam.access-token-ttl:PT1H}")
    private Duration ttl;

    @Override
    public IssuedToken issueFor(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cloud-api")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getDisplayName())
                .claim("roles", user.getRoles().stream().map(Role::toCode).toList())
                .build();

        String value = encoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(() -> "RS256").build(), claims))
                .getTokenValue();

        return new IssuedToken(value, ttl.toSeconds());
    }
}
