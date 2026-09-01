package com.pe.cloudapi.shared.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * El puente entre los rechazos de Spring Security y el manejador global.
 *
 * <p>La seguridad rechaza antes del {@code DispatcherServlet}, así que el
 * {@code @RestControllerAdvice} no se enteraría. Esto le devuelve la excepción a
 * Spring MVC para que le dé forma {@link GlobalExceptionHandler}, y así el
 * formato del error se escribe en un solo sitio.
 */
@Component
public class SecurityErrorResponder implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    /**
     * El constructor va a mano y no con Lombok: {@code @RequiredArgsConstructor}
     * no copia {@code @Qualifier} al parametro que genera, y hay dos
     * {@link HandlerExceptionResolver} en el contexto.
     */
    public SecurityErrorResponder(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /** Falta la credencial o no vale. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) {
        resolver.resolveException(request, response, null, exception);
    }

    /** La credencial vale y no alcanza. */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) {
        resolver.resolveException(request, response, null, exception);
    }
}
