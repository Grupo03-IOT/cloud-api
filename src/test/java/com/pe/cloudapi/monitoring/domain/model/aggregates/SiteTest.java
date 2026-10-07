package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Site aggregate")
class SiteTest {

    @Test
    @DisplayName("defaults the timezone to America/Lima when none is given")
    void defaultsTimezoneToLima() {
        Site site = new Site(new CreateSiteCommand(
                "coworking-lima-centro", "Coworking Lima Centro", "Av. Arequipa 123", null));

        assertThat(site.getCode()).isEqualTo("coworking-lima-centro");
        assertThat(site.getTimezone()).isEqualTo("America/Lima");
    }
}
