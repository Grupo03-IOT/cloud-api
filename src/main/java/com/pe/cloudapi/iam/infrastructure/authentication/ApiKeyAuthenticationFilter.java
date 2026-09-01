package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateApiKeyUseCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Autentica a las máquinas por su clave, sin canjearla por nada.
 *
 * <p>Lee {@code X-API-Key} y consulta la base en cada petición, así que revocar
 * surte efecto de inmediato.
 *
 * <p>Con una clave inválida <strong>no responde</strong>: deja la petición sin
 * autenticar y deciden las reglas de autorización.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final AuthenticateApiKeyUseCase authenticateApiKey;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        apiKeyOf(request).flatMap(authenticateApiKey::execute).ifPresent(credential -> {
            var authorities = credential.getScopes().stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope.toCode()))
                    .toList();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            credential.getCode(), null, authorities));
        });

        chain.doFilter(request, response);
    }

    private Optional<String> apiKeyOf(HttpServletRequest request) {
        String value = request.getHeader(HEADER);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }
}
