package com.pe.cloudapi.iam.domain.model.commands;

/**
 * Lo que un visitante envía para pedir una demostración, ya con el
 * consentimiento aceptado.
 */
public record RequestDemoCommand(String fullName, String workEmail, String company, int roomCount) {
}
