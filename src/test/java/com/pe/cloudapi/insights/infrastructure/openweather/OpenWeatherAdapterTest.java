package com.pe.cloudapi.insights.infrastructure.openweather;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("OpenWeather adapter")
class OpenWeatherAdapterTest {

    private static final String URL =
            "https://api.openweathermap.org/data/2.5/weather?appid=SECRET-KEY&units=metric&id=3936456";

    @Test
    @DisplayName("returns nothing and logs the status, never the key, when the provider rejects it")
    void keepsTheKeyOutOfTheLogs(CapturedOutput output) {
        OpenWeatherClient client = mock(OpenWeatherClient.class);
        Request request = Request.create(Request.HttpMethod.GET, URL, Map.of(), null, StandardCharsets.UTF_8, null);
        when(client.currentWeather()).thenThrow(
                new FeignException.Unauthorized("[401 Unauthorized] during [GET] to [" + URL + "]", request, null, Map.of()));

        assertThat(new OpenWeatherAdapter(client).fetchCurrent()).isEmpty();
        assertThat(output).contains("HTTP 401").doesNotContain("SECRET-KEY");
    }
}
