package com.pe.cloudapi.alerting.domain.model.commands;

import java.util.UUID;

/**
 * El administrador reconoce una alerta y deja escrita la acción que tomó.
 *
 * @param alertId          alerta reconocida
 * @param userId           quién la reconoce; sale del token, nunca del cuerpo
 * @param correctiveAction qué se hizo, de 3 a 500 caracteres
 */
public record AcknowledgeAlertCommand(UUID alertId, UUID userId, String correctiveAction) {
}
