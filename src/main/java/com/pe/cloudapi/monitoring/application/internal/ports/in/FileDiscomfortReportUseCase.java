package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;

/**
 * Puerto de entrada: un miembro reporta una molestia en una sala.
 */
public interface FileDiscomfortReportUseCase {

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_ROOM_NOT_FOUND} o {@code MONITORING_DISCOMFORT_TOO_FREQUENT}
     */
    RoomDiscomfortReport execute(FileDiscomfortReportCommand command);
}
