package com.pe.cloudapi.monitoring.application.internal.results;

import java.util.UUID;

/**
 * Cuántos reportes de cada motivo tiene una sala: con esto el administrador
 * prioriza dónde actuar (US28).
 */
public record DiscomfortSummary(UUID roomId, String roomCode, long noise, long warm, long cold,
                                long discrepancies) {

    public long total() {
        return noise + warm + cold;
    }
}
