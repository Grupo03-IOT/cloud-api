package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.shared.interfaces.rest.SecurityErrorResponder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * La cadena de filtros que atraviesa toda petición antes de llegar a un
 * controlador.
 *
 * <p>Sin estado: cada petición trae su credencial. Por eso CSRF está
 * desactivado — protege formularios con sesión, y aquí no hay ninguna.
 *
 * <p><strong>Todo abierto todavía</strong>: las reglas por endpoint llegan
 * después.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ApiKeyAuthenticationFilter apiKeyFilter;
    private final SecurityErrorResponder securityErrors;

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Spring Security responde antes de llegar a un controlador, asi
                // que el manejador global no se entera: sin esto, un 401 saldria
                // con el cuerpo por defecto en vez de con el formato de la API.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityErrors)
                        .accessDeniedHandler(securityErrors))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(securityErrors)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolesConverter())))
                // Antes de la cadena de autenticacion: si la peticion trae una
                // clave de maquina, llega identificada; si no, sigue su camino y
                // decide el JWT.
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * De la reclamación {@code roles} a autoridades {@code ROLE_*}.
     *
     * <p>Sin esto Spring leería la reclamación {@code scope}, que en los tokens
     * de persona no existe, y nadie tendría ninguna autoridad.
     */
    private JwtAuthenticationConverter rolesConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /** Lo usa {@code BCryptPasswordHasher}. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
