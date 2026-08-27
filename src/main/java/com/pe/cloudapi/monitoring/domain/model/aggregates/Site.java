package com.pe.cloudapi.monitoring.domain.model.aggregates;

import com.pe.cloudapi.monitoring.domain.model.commands.CreateSiteCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Local físico donde se instalan los dispositivos: el coworking.
 *
 * <p>Hoy existe una sola instancia. El agregado se modela desde el principio
 * para que atender varios locales más adelante no obligue a una migración.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Site {

    private static final String DEFAULT_TIMEZONE = "America/Lima";

    private final UUID id;
    private final String code;
    @Setter private String name;
    @Setter private String address;
    @Setter private String timezone;

    public Site(CreateSiteCommand command) {
        this.id = null;
        this.code = command.code();
        this.name = command.name();
        this.address = command.address();
        this.timezone = command.timezone() == null ? DEFAULT_TIMEZONE : command.timezone();
    }
}
