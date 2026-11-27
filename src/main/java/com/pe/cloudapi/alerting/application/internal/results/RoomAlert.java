package com.pe.cloudapi.alerting.application.internal.results;

import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;

/**
 * Una alerta con el código actual de su sala, que es lo que muestran las apps.
 *
 * @param alert    la alerta
 * @param roomCode código de la sala; {@code null} si la sala ya no está activa
 */
public record RoomAlert(Alert alert, String roomCode) {
}
