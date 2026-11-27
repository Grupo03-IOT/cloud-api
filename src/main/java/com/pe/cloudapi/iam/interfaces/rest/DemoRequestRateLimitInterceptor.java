package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de envíos del formulario público por IP, en ventanas fijas y en
 * memoria: se reinicia con la aplicación y cada réplica lleva su propia cuenta.
 *
 * <p>Cuenta todo {@code POST}, también los que luego no pasan la validación.
 */
@Component
public class DemoRequestRateLimitInterceptor implements HandlerInterceptor {

    /** A partir de aquí se descartan las ventanas vencidas para no crecer sin fin. */
    private static final int PURGE_THRESHOLD = 1_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final Duration length;
    private final Clock clock;

    @Autowired
    public DemoRequestRateLimitInterceptor(
            @Value("${app.demo-requests.rate-limit.max-requests:5}") int maxRequests,
            @Value("${app.demo-requests.rate-limit.window:PT10M}") Duration length) {
        this(maxRequests, length, Clock.systemUTC());
    }

    DemoRequestRateLimitInterceptor(int maxRequests, Duration length, Clock clock) {
        this.maxRequests = maxRequests;
        this.length = length;
        this.clock = clock;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        Instant now = clock.instant();
        if (windows.size() >= PURGE_THRESHOLD) {
            windows.values().removeIf(window -> window.isOverAt(now));
        }
        Window current = windows.compute(clientIp(request), (ip, window) ->
                window == null || window.isOverAt(now)
                        ? new Window(now.plus(length), 1)
                        : new Window(window.endsAt(), window.count() + 1));
        if (current.count() > maxRequests) {
            long retryAfter = Math.max(1, Duration.between(now, current.endsAt()).toSeconds());
            response.setHeader("Retry-After", Long.toString(retryAfter));
            throw IamError.DEMO_REQUEST_LIMIT_REACHED.with();
        }
        return true;
    }

    /**
     * Detrás del proxy de la plataforma la IP real es la <strong>última</strong>
     * de {@code X-Forwarded-For}, la que agrega el proxy; las anteriores las
     * puede inventar quien llama.
     */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded == null || forwarded.isBlank()) {
            return request.getRemoteAddr();
        }
        return forwarded.substring(forwarded.lastIndexOf(',') + 1).strip();
    }

    private record Window(Instant endsAt, int count) {

        boolean isOverAt(Instant at) {
            return !at.isBefore(endsAt);
        }
    }
}
