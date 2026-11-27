package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.application.internal.ports.in.IsUserActiveUseCase;
import com.pe.cloudapi.iam.domain.model.events.UserActiveChangedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("US10 - account status cache")
class UserStatusCacheTest {

    private final IsUserActiveUseCase isUserActive = mock(IsUserActiveUseCase.class);
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-26T12:00:00Z"));
    private final UserStatusCache cache = new UserStatusCache(isUserActive, Duration.ofSeconds(30), clock);
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("asks once per TTL window, then again after it expires")
    void cachesWithinTtl() {
        when(isUserActive.execute(userId)).thenReturn(true, false);

        assertThat(cache.isActive(userId)).isTrue();
        clock.advance(Duration.ofSeconds(29));
        assertThat(cache.isActive(userId)).isTrue();
        verify(isUserActive, times(1)).execute(userId);

        clock.advance(Duration.ofSeconds(1));
        assertThat(cache.isActive(userId)).isFalse();
        verify(isUserActive, times(2)).execute(userId);
    }

    @Test
    @DisplayName("a deactivation event drops the cached status at once")
    void evictsOnChange() {
        when(isUserActive.execute(userId)).thenReturn(true, false);
        assertThat(cache.isActive(userId)).isTrue();

        cache.on(new UserActiveChangedEvent(userId, false));

        assertThat(cache.isActive(userId)).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
