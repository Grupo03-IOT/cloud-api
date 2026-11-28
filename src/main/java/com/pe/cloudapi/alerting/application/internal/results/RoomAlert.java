package com.pe.cloudapi.alerting.application.internal.results;

import com.pe.cloudapi.alerting.domain.model.aggregates.Alert;

/**
 * Una alerta con lo que muestran las apps además de sus propios datos.
 *
 * @param alert    la alerta
 * @param roomCode código de la sala; {@code null} si la sala ya no está activa
 * @param acknowledgedByName nombre completo de quien la reconoció; {@code null}
 *                           si nadie la reconoció o la cuenta ya no existe
 */
public record RoomAlert(Alert alert, String roomCode, String acknowledgedByName) {
}
