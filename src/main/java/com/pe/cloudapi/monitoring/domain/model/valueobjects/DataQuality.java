package com.pe.cloudapi.monitoring.domain.model.valueobjects;

/**
 * Indica cuánto dato crudo respalda un agregado, para poder distinguir una
 * lectura sólida de otra construida con los pocos lotes que sobrevivieron.
 *
 * <p>El dispositivo envía un lote cada diez segundos, así que un minuto
 * completo se apoya en seis. Menos significa que se perdieron por el camino.
 *
 * @param batches  lotes realmente recibidos en el periodo
 * @param expected lotes que deberían haber llegado
 */
public record DataQuality(Integer batches, Integer expected) {

    /**
     * Calidad desconocida, para lecturas que no informan cuántos lotes las
     * respaldan.
     *
     * @return calidad con ambos recuentos nulos
     */
    public static DataQuality unknown() {
        return new DataQuality(null, null);
    }

    /**
     * Indica si llegaron todos los lotes previstos para el periodo.
     *
     * @return {@code false} también cuando se desconoce el recuento
     */
    public boolean isComplete() {
        return batches != null && expected != null && batches >= expected;
    }

    /**
     * Cuántos lotes faltaron.
     *
     * @return 0 si llegaron todos o si se desconoce el recuento
     */
    public int missingBatches() {
        if (batches == null || expected == null) return 0;
        return Math.max(0, expected - batches);
    }
}
