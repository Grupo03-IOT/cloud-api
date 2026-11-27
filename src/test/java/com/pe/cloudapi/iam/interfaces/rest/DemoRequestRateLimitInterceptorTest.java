package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Per-IP limit of the public demo form")
class DemoRequestRateLimitInterceptorTest {

    private static final Instant START = Instant.parse("2026-11-27T15:00:00Z");

    private final Clock clock = mock(Clock.class);
    private final DemoRequestRateLimitInterceptor limit =
            new DemoRequestRateLimitInterceptor(2, Duration.ofMinutes(10), clock);

    @Test
    @DisplayName("blocks the third request of the window and allows again when it ends")
    void window() {
        when(clock.instant()).thenReturn(START);
        assertThat(send("10.0.0.1", null)).isTrue();
        assertThat(send("10.0.0.1", null)).isTrue();

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> limit.preHandle(post("10.0.0.1", null), response, this))
                .extracting(ex -> ((DomainException) ex).getError())
                .isEqualTo(IamError.DEMO_REQUEST_LIMIT_REACHED);
        assertThat(response.getHeader("Retry-After")).isEqualTo("600");
        assertThat(send("10.0.0.2", null)).isTrue();

        when(clock.instant()).thenReturn(START.plus(Duration.ofMinutes(10)));
        assertThat(send("10.0.0.1", null)).isTrue();
    }

    @Test
    @DisplayName("behind a proxy it counts the last forwarded address, not the spoofable ones")
    void forwarded() {
        when(clock.instant()).thenReturn(START);
        send("100.100.0.1", "1.1.1.1, 203.0.113.7");
        send("100.100.0.1", "2.2.2.2, 203.0.113.7");

        assertThatThrownBy(() -> send("100.100.0.1", "3.3.3.3, 203.0.113.7"))
                .isInstanceOf(DomainException.class);
        assertThat(send("100.100.0.1", "203.0.113.8")).isTrue();
    }

    @Test
    @DisplayName("does not limit reads")
    void reads() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            assertThat(limit.preHandle(new MockHttpServletRequest("GET", "/api/v1/demo-requests"),
                    new MockHttpServletResponse(), this)).isTrue();
        }
    }

    private boolean send(String remoteAddress, String forwardedFor) {
        return limit.preHandle(post(remoteAddress, forwardedFor), new MockHttpServletResponse(), this);
    }

    private static MockHttpServletRequest post(String remoteAddress, String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/demo-requests");
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        return request;
    }
}
