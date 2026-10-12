package com.pe.cloudapi.monitoring.application.internal.ports.in;

/**
 * Puerto de entrada: registrar el latido de un Edge.
 */
public interface RecordHeartbeatUseCase {

    /**
     * Da de alta el Edge si es su primer latido.
     *
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code MONITORING_SITE_NOT_FOUND} si no hay ningún local con ese código
     */
    void execute(String code, String siteCode, String version, int pendingUpload, int roomsReported);
}
