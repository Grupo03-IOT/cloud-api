package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.application.internal.ports.in.IsUserActiveUseCase;
import com.pe.cloudapi.iam.domain.model.events.UserActiveChangedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Si una cuenta sigue activa, recordado unos segundos por usuario.
 *
 * <p>Cada petición con token pasa por aquí; sin la caché serían una consulta
 * por petición. Una desactivación hecha en esta instancia se aplica al
 * momento (se escucha el evento); en otra réplica, a más tardar al vencer el
 * {@code app.iam.user-status-ttl}.
 */
@Component
public class UserStatusCache {

    private record Entry(boolean active, Instant expiresAt) {
    }

    private final IsUserActiveUseCase isUserActive;
    private final Duration ttl;
    private final Clock clock;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    @Autowired
    public UserStatusCache(IsUserActiveUseCase isUserActive,
                           @Value("${app.iam.user-status-ttl:PT30S}") Duration ttl) {
        this(isUserActive, ttl, Clock.systemUTC());
    }

    UserStatusCache(IsUserActiveUseCase isUserActive, Duration ttl, Clock clock) {
        this.isUserActive = isUserActive;
        this.ttl = ttl;
        this.clock = clock;
    }

    public boolean isActive(UUID userId) {
        Instant now = clock.instant();
        Entry entry = entries.get(userId);
        if (entry == null || !now.isBefore(entry.expiresAt())) {
            entry = new Entry(isUserActive.execute(userId), now.plus(ttl));
            entries.put(userId, entry);
        }
        return entry.active();
    }

    /** Tras confirmarse el cambio, para no volver a guardar el estado viejo. */
    @TransactionalEventListener(fallbackExecution = true)
    public void on(UserActiveChangedEvent event) {
        entries.remove(event.userId());
    }
}
