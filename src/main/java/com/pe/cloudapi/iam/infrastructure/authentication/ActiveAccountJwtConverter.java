package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.shared.interfaces.rest.CatalogAuthenticationException;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Del token de persona a la autenticación, comprobando que la cuenta siga activa.
 *
 * <p>Un JWT vale hasta que vence aunque la cuenta se desactive después. Por
 * eso, además de la firma, se mira el estado de la cuenta en cada petición: si
 * está desactivada, {@code 401 IAM_ACCOUNT_DISABLED}.
 *
 * <p>Las autoridades salen de la reclamación {@code roles} con prefijo
 * {@code ROLE_}. Sin eso Spring leería {@code scope}, que en los tokens de
 * persona no existe, y nadie tendría ninguna autoridad.
 */
@Component
@RequiredArgsConstructor
public class ActiveAccountJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserStatusCache userStatus;
    private final JwtAuthenticationConverter roles = rolesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (!userStatus.isActive(userIdOf(jwt))) {
            throw new CatalogAuthenticationException(IamError.ACCOUNT_DISABLED.with());
        }
        return roles.convert(jwt);
    }

    private static UUID userIdOf(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidBearerTokenException("The token subject is not a user id");
        }
    }

    private static JwtAuthenticationConverter rolesConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
