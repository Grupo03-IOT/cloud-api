package com.pe.cloudapi.iam.domain.model.aggregates;

import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Solicitud de demostración de una organización que todavía no tiene cuenta
 * (US06). Es inmutable: solo se registra y se consulta.
 */
@Getter
@Builder
@AllArgsConstructor
public class DemoRequest {

    private final UUID id;
    private final String fullName;
    private final String workEmail;
    private final String company;
    private final int roomCount;
    private final OffsetDateTime consentedAt;
    private final OffsetDateTime createdAt;

    /**
     * @param command datos del formulario
     * @param now     momento de la solicitud, que es también el del consentimiento
     */
    public DemoRequest(RequestDemoCommand command, OffsetDateTime now) {
        this.id = null;
        this.fullName = command.fullName().strip();
        this.workEmail = command.workEmail().strip().toLowerCase(Locale.ROOT);
        this.company = command.company().strip();
        this.roomCount = command.roomCount();
        this.consentedAt = now;
        this.createdAt = now;
    }
}
