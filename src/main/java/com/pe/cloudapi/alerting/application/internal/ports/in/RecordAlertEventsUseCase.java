package com.pe.cloudapi.alerting.application.internal.ports.in;

import com.pe.cloudapi.alerting.domain.model.commands.RecordAlertEventCommand;

import java.util.List;

/**
 * Puerto de entrada: guardar los eventos de alerta que sube el Edge.
 *
 * <p>Idempotente por {@code sourceKey}: un evento repetido actualiza, no duplica.
 */
public interface RecordAlertEventsUseCase {

    /**
     * @param events eventos del lote, al menos uno
     * @return cuántos se aceptaron
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} si alguna sala no existe; en ese
     *         caso no se guarda ninguno
     */
    int execute(List<RecordAlertEventCommand> events);
}
