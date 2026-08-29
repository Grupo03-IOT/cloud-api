package com.pe.cloudapi.insights.domain.services;

import com.pe.cloudapi.insights.domain.model.results.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.Correlation;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.model.valueobjects.Trend;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Los cálculos estadísticos sobre una serie de lecturas.
 *
 * <p>Es un servicio de dominio: no consulta nada ni guarda nada, recibe la
 * serie y devuelve conclusiones. Eso lo hace probable sin base de datos.
 */
@Service
public class ComfortAnalyticsService {

    /** Muestras mínimas para que cualquier cálculo signifique algo. */
    public static final int MINIMUM_SAMPLE = 30;

    /** Desviaciones típicas por encima de la media para considerar un pico anómalo. */
    private static final double ANOMALY_Z_SCORE = 3.0;

    private static final long MILLIS_PER_HOUR = Duration.ofHours(1).toMillis();

    public RoomAnalytics analyze(UUID roomId, OffsetDateTime from, OffsetDateTime to,
                                 List<ReadingPoint> series) {
        return new RoomAnalytics(
                roomId, from, to, series.size(),
                noiseVsOccupancy(series),
                thermalDrift(series),
                noiseAnomalies(series));
    }

    /**
     * ¿El ruido sube cuando hay gente, o la sala es ruidosa por sí misma?
     *
     * <p>Una correlación alta señala a los ocupantes; una baja con niveles
     * altos apunta al aire acondicionado, a la calle o a un equipo zumbando —
     * que es un problema que la administración puede arreglar.
     */
    private Correlation noiseVsOccupancy(List<ReadingPoint> series) {
        List<ReadingPoint> usable = series.stream()
                .filter(p -> p.laeq() != null && p.occupiedPct() != null)
                .toList();
        if (usable.size() < MINIMUM_SAMPLE) {
            return Correlation.insufficientData(usable.size());
        }
        double[] noise = usable.stream().mapToDouble(ReadingPoint::laeq).toArray();
        double[] occupancy = usable.stream().mapToDouble(ReadingPoint::occupiedPct).toArray();
        double coefficient = new PearsonsCorrelation().correlation(noise, occupancy);
        return Double.isNaN(coefficient)
                ? Correlation.insufficientData(usable.size())
                : new Correlation(coefficient, usable.size());
    }

    /**
     * Cuánto se calienta la sala por hora.
     *
     * <p>Una pendiente alta con la sala ocupada delata climatización que no da
     * abasto. La regresión se hace sobre milisegundos y el resultado se
     * reescala a horas, que es la unidad en que la respuesta es legible.
     */
    private Trend thermalDrift(List<ReadingPoint> series) {
        List<ReadingPoint> usable = series.stream()
                .filter(p -> p.tempC() != null)
                .toList();
        if (usable.size() < MINIMUM_SAMPLE) {
            return Trend.insufficientData(usable.size());
        }
        SimpleRegression regression = new SimpleRegression();
        usable.forEach(p -> regression.addData(
                p.ts().toInstant().toEpochMilli(), p.tempC()));

        double slopePerMilli = regression.getSlope();
        double rSquared = regression.getRSquare();
        if (Double.isNaN(slopePerMilli) || Double.isNaN(rSquared)) {
            return Trend.insufficientData(usable.size());
        }
        return new Trend(slopePerMilli * MILLIS_PER_HOUR, rSquared, usable.size());
    }

    /**
     * Minutos cuyo nivel se aparta más de {@value #ANOMALY_Z_SCORE} desviaciones
     * típicas de la media del periodo.
     *
     * <p>Es una detección relativa a la propia sala, no a un umbral fijo: lo
     * anómalo en una cabina de llamadas es rutina en una zona común.
     */
    private List<OffsetDateTime> noiseAnomalies(List<ReadingPoint> series) {
        List<ReadingPoint> usable = series.stream()
                .filter(p -> p.laeq() != null)
                .toList();
        if (usable.size() < MINIMUM_SAMPLE) {
            return List.of();
        }
        DescriptiveStatistics stats = new DescriptiveStatistics();
        usable.forEach(p -> stats.addValue(p.laeq()));

        double mean = stats.getMean();
        double deviation = stats.getStandardDeviation();
        if (deviation == 0) {
            return List.of();
        }
        List<OffsetDateTime> anomalies = new ArrayList<>();
        for (ReadingPoint point : usable) {
            if ((point.laeq() - mean) / deviation > ANOMALY_Z_SCORE) {
                anomalies.add(point.ts());
            }
        }
        return List.copyOf(anomalies);
    }
}
