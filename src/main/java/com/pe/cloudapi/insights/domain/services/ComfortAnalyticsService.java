package com.pe.cloudapi.insights.domain.services;

import com.pe.cloudapi.insights.domain.model.aggregates.WeatherObservation;
import com.pe.cloudapi.insights.domain.model.valueobjects.Comparison;
import com.pe.cloudapi.insights.domain.model.valueobjects.Correlation;
import com.pe.cloudapi.insights.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.insights.domain.model.valueobjects.Finding;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourSlot;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourlyProfile;
import com.pe.cloudapi.insights.domain.model.valueobjects.Measure;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseDistribution;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisode;
import com.pe.cloudapi.insights.domain.model.valueobjects.NoiseEpisodes;
import com.pe.cloudapi.insights.domain.model.valueobjects.Period;
import com.pe.cloudapi.insights.domain.model.valueobjects.PeriodWindow;
import com.pe.cloudapi.insights.domain.model.valueobjects.ReadingPoint;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomAnalytics;
import com.pe.cloudapi.insights.domain.model.valueobjects.RoomRanking;
import com.pe.cloudapi.insights.domain.model.valueobjects.Trend;
import com.pe.cloudapi.insights.domain.model.valueobjects.Verdict;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final long MILLIS_PER_HOUR = Duration.ofHours(1).toMillis();

    /** Distancia máxima entre una observación exterior y la lectura interior con que se empareja. */
    private static final Duration PAIRING_TOLERANCE = Duration.ofMinutes(5);

    /** z robusto, sobre mediana y MAD, por encima del cual un minuto es pico. */
    private static final double ANOMALY_ROBUST_Z = 3.5;

    /** Por debajo de este exceso sobre la mediana, el cambio no se distingue al oído. */
    private static final double MINIMUM_PEAK_EXCESS_DB = 6.0;

    /** Hace la MAD comparable a una desviación típica en una distribución normal. */
    private static final double MAD_CONSISTENCY = 0.6745;

    /** Dos picos a esta distancia o menos son el mismo episodio: un hueco de dos minutos no lo parte. */
    private static final Duration EPISODE_MAX_GAP = Duration.ofMinutes(3);

    private static final int WORST_EPISODES = 5;

    /** Por debajo, la correlación se lee como ausencia de relación. */
    private static final double WEAK_CORRELATION = 0.3;

    /** Desde aquí una correlación concluyente se cuenta como hallazgo. */
    private static final double FINDING_CORRELATION = 0.5;

    /** Una deriva menor que esta, en °C por hora, es temperatura estable. */
    private static final double STABLE_DRIFT_PER_HOUR = 0.1;

    /** Desde aquí, el tiempo por encima del umbral se cuenta como hallazgo. */
    private static final double FINDING_PCT_OVER = 10.0;

    private static final String UNIT_PEARSON = "pearson_r";
    private static final String UNIT_DRIFT = "degc_per_hour";
    private static final String UNIT_PCT_OF_TIME = "pct_of_time";

    private static final Comparator<RoomRanking> WORST_FIRST =
            Comparator.comparing(ComfortAnalyticsService::hasNoNoiseData)
                    .thenComparing(ComfortAnalyticsService::pctOverOf,
                            Comparator.nullsLast(Comparator.<Double>reverseOrder()))
                    .thenComparing(ComfortAnalyticsService::p90Of,
                            Comparator.nullsLast(Comparator.<Double>reverseOrder()));

    /**
     * Todo lo que se puede afirmar de una sala en un periodo, salvo la
     * comparación con el anterior, que añade {@link #compare}.
     *
     * @param noiseThreshold umbral de aviso de {@code laeq}; nulo si la sala no tiene
     */
    public RoomAnalytics analyze(UUID roomId, PeriodWindow window, List<ReadingPoint> series,
                                 List<WeatherObservation> outdoor, Float noiseThreshold) {
        Measure noise = correlationMeasure(noiseVsOccupancy(series),
                "noise_follows_occupancy", "noise_without_occupancy", "noise_independent_of_occupancy");
        Measure drift = thermalDriftMeasure(thermalDrift(series));
        Measure insulation = correlationMeasure(indoorVsOutdoor(series, outdoor),
                "follows_outdoor_temperature", "opposes_outdoor_temperature", "holds_own_temperature");
        NoiseDistribution distribution = noiseDistribution(series, noiseThreshold);

        return new RoomAnalytics(roomId, window, dataQuality(window, series),
                findings(noise, drift, insulation, distribution),
                noise, drift, insulation, noiseEpisodes(series), distribution, null);
    }

    /**
     * @return nula si alguno de los dos periodos no tiene lecturas de ruido
     */
    public Comparison compare(NoiseDistribution current, Period previous,
                              List<ReadingPoint> previousSeries, Float noiseThreshold) {
        NoiseDistribution before = noiseDistribution(previousSeries, noiseThreshold);
        if (current == null || before == null) {
            return null;
        }
        Double pctOverDelta = current.pctOver() == null || before.pctOver() == null
                ? null
                : round(current.pctOver() - before.pctOver());
        return new Comparison(previous.label(), pctOverDelta, round(current.p90() - before.p90()));
    }

    public DataQuality dataQuality(PeriodWindow window, List<ReadingPoint> series) {
        return DataQuality.of(window.minutes(), series.size());
    }

    /**
     * ¿El ruido sube cuando hay gente, o la sala es ruidosa por sí misma?
     *
     * <p>Una correlación alta señala a los ocupantes; una baja con niveles
     * altos apunta al aire acondicionado, a la calle o a un equipo zumbando —
     * que es un problema que la administración puede arreglar.
     */
    public Correlation noiseVsOccupancy(List<ReadingPoint> series) {
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
     * <p>La regresión se hace sobre milisegundos y el resultado se reescala a
     * horas, que es la unidad en que la respuesta es legible.
     */
    public Trend thermalDrift(List<ReadingPoint> series) {
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
     * ¿La temperatura de la sala sigue a la de la calle?
     *
     * <p>Una correlación alta significa que la sala está a merced del exterior:
     * mal aislada, o con climatización insuficiente. Una baja significa que
     * mantiene su temperatura por su cuenta, que es lo deseable.
     *
     * <p>Cada observación exterior se empareja con la lectura interior más
     * cercana, siempre que esté a {@link #PAIRING_TOLERANCE} o menos; la que no
     * tiene ninguna tan cerca se descarta. {@code sampleSize} cuenta los pares.
     */
    public Correlation indoorVsOutdoor(List<ReadingPoint> series, List<WeatherObservation> outdoor) {
        List<ReadingPoint> usableIndoor = series.stream()
                .filter(p -> p.tempC() != null)
                .sorted(Comparator.comparing(ReadingPoint::ts))
                .toList();
        List<WeatherObservation> usableOutdoor = outdoor.stream()
                .filter(o -> o.getTempC() != null)
                .sorted(Comparator.comparing(WeatherObservation::getObservedAt))
                .toList();

        List<TemperaturePair> pairs = pairWithinTolerance(usableIndoor, usableOutdoor);
        if (pairs.size() < MINIMUM_SAMPLE) {
            return Correlation.insufficientData(pairs.size());
        }

        double[] inside = pairs.stream().mapToDouble(TemperaturePair::indoor).toArray();
        double[] outside = pairs.stream().mapToDouble(TemperaturePair::outdoor).toArray();
        double coefficient = new PearsonsCorrelation().correlation(inside, outside);
        return Double.isNaN(coefficient)
                ? Correlation.insufficientData(pairs.size())
                : new Correlation(coefficient, pairs.size());
    }

    /**
     * Minutos con un pico de ruido fuera de lo normal para la propia sala,
     * ordenados de más antiguo a más reciente.
     *
     * <p>Cada minuto se compara con los de su mismo estado —sala vacía u
     * ocupada— y solo hacia arriba: es pico si su z robusto sobre la mediana y
     * la MAD de su estado pasa de {@value #ANOMALY_ROBUST_Z} y además la supera
     * en al menos {@value #MINIMUM_PEAK_EXCESS_DB} dB. Un estado con menos de
     * {@value #MINIMUM_SAMPLE} minutos no se evalúa.
     */
    public List<ReadingPoint> noisePeaks(List<ReadingPoint> series) {
        Map<Boolean, List<ReadingPoint>> byOccupancy = series.stream()
                .filter(p -> p.laeq() != null && p.occupiedPct() != null)
                .collect(Collectors.partitioningBy(p -> p.occupiedPct() > 0));

        return byOccupancy.values().stream()
                .filter(points -> points.size() >= MINIMUM_SAMPLE)
                .flatMap(points -> peaksAboveOwnBaseline(points).stream())
                .sorted(Comparator.comparing(ReadingPoint::ts))
                .toList();
    }

    /**
     * Los picos de {@link #noisePeaks} fundidos en episodios: dos picos a
     * {@link #EPISODE_MAX_GAP} o menos pertenecen al mismo.
     */
    public NoiseEpisodes noiseEpisodes(List<ReadingPoint> series) {
        List<NoiseEpisode> episodes = new ArrayList<>();
        List<ReadingPoint> current = new ArrayList<>();
        for (ReadingPoint peak : noisePeaks(series)) {
            if (!current.isEmpty()
                    && Duration.between(current.getLast().ts(), peak.ts()).compareTo(EPISODE_MAX_GAP) > 0) {
                episodes.add(episodeOf(current));
                current = new ArrayList<>();
            }
            current.add(peak);
        }
        if (!current.isEmpty()) {
            episodes.add(episodeOf(current));
        }

        List<NoiseEpisode> worst = episodes.stream()
                .sorted(Comparator.comparingDouble(NoiseEpisode::peakLaeq).reversed())
                .limit(WORST_EPISODES)
                .toList();
        return new NoiseEpisodes(episodes.size(), worst);
    }

    /**
     * Percentiles del nivel sonoro y, si hay umbral, el tiempo por encima.
     *
     * @return nula si la serie no tiene lecturas de ruido
     */
    public NoiseDistribution noiseDistribution(List<ReadingPoint> series, Float noiseThreshold) {
        double[] levels = series.stream()
                .filter(p -> p.laeq() != null)
                .mapToDouble(ReadingPoint::laeq)
                .toArray();
        if (levels.length == 0) {
            return null;
        }

        Percentile percentile = new Percentile();
        percentile.setData(levels);
        double p50 = round(percentile.evaluate(50));
        double p90 = round(percentile.evaluate(90));
        double p95 = round(percentile.evaluate(95));
        if (noiseThreshold == null) {
            return new NoiseDistribution(p50, p90, p95, levels.length, null, null, null);
        }

        int minutesOver = (int) Arrays.stream(levels).filter(level -> level > noiseThreshold).count();
        return new NoiseDistribution(p50, p90, p95, levels.length, noiseThreshold,
                minutesOver, round(minutesOver * 100.0 / levels.length));
    }

    /**
     * Las 24 horas del día en la zona del local, cada una con lo que se oyó
     * en ella a lo largo del periodo.
     */
    public HourlyProfile hourlyProfile(UUID roomId, PeriodWindow window, List<ReadingPoint> series) {
        Map<Integer, List<ReadingPoint>> byHour = series.stream()
                .filter(p -> p.laeq() != null)
                .collect(Collectors.groupingBy(p -> p.ts().atZoneSameInstant(window.zone()).getHour()));

        List<HourSlot> hours = IntStream.range(0, 24)
                .mapToObj(hour -> hourSlot(hour, byHour.getOrDefault(hour, List.of())))
                .toList();
        return new HourlyProfile(roomId, window, hours);
    }

    /**
     * Las salas de peor a mejor: primero las que más tiempo pasaron sobre su
     * umbral, después las que no tienen umbral por su p90, y al final las que
     * no tienen datos.
     */
    public List<RoomRanking> rank(List<RoomRanking> rooms) {
        return rooms.stream().sorted(WORST_FIRST).toList();
    }

    /**
     * Media de niveles sonoros por energía: {@code 10·log10(media(10^(L/10)))}.
     * La media aritmética de decibelios subestima los picos.
     */
    static double energyAverage(double... levels) {
        double meanEnergy = Arrays.stream(levels).map(level -> Math.pow(10, level / 10)).average().orElseThrow();
        return 10 * Math.log10(meanEnergy);
    }

    private Measure correlationMeasure(Correlation correlation, String positiveCode,
                                       String negativeCode, String weakCode) {
        if (correlation.verdict() != Verdict.CONCLUSIVE) {
            return Measure.withoutConclusion(correlation.verdict(), UNIT_PEARSON, correlation.sampleSize(), null);
        }
        double coefficient = correlation.coefficient();
        String code = Math.abs(coefficient) < WEAK_CORRELATION ? weakCode
                : coefficient > 0 ? positiveCode : negativeCode;
        return new Measure(Verdict.CONCLUSIVE, code, coefficient, UNIT_PEARSON,
                correlation.direction(), correlation.sampleSize(), null);
    }

    private Measure thermalDriftMeasure(Trend trend) {
        if (trend.verdict() != Verdict.CONCLUSIVE) {
            return Measure.withoutConclusion(trend.verdict(), UNIT_DRIFT, trend.sampleSize(), trend.rSquared());
        }
        double slope = trend.slopePerHour();
        String code = Math.abs(slope) < STABLE_DRIFT_PER_HOUR ? "stable_temperature"
                : slope > 0 ? "warming_over_period" : "cooling_over_period";
        return new Measure(Verdict.CONCLUSIVE, code, slope, UNIT_DRIFT,
                trend.direction(), trend.sampleSize(), trend.rSquared());
    }

    private List<Finding> findings(Measure noise, Measure drift, Measure insulation,
                                   NoiseDistribution distribution) {
        List<Finding> findings = new ArrayList<>();
        if (noise.isConclusive() && Math.abs(noise.value()) >= FINDING_CORRELATION) {
            findings.add(Finding.of(noise));
        }
        if (insulation.isConclusive() && insulation.value() >= FINDING_CORRELATION) {
            findings.add(Finding.of(insulation));
        }
        if (drift.isConclusive() && Math.abs(drift.value()) >= STABLE_DRIFT_PER_HOUR) {
            findings.add(Finding.of(drift));
        }
        if (distribution != null && distribution.pctOver() != null
                && distribution.pctOver() >= FINDING_PCT_OVER) {
            findings.add(new Finding("often_above_threshold", distribution.pctOver(), UNIT_PCT_OF_TIME, null));
        }
        return List.copyOf(findings);
    }

    /** Las dos listas deben venir ordenadas por tiempo: se recorren en una sola pasada. */
    private List<TemperaturePair> pairWithinTolerance(List<ReadingPoint> indoor,
                                                      List<WeatherObservation> outdoor) {
        List<TemperaturePair> pairs = new ArrayList<>();
        if (indoor.isEmpty()) {
            return pairs;
        }
        int nearest = 0;
        for (WeatherObservation observation : outdoor) {
            OffsetDateTime instant = observation.getObservedAt();
            while (nearest + 1 < indoor.size()
                    && distance(indoor.get(nearest + 1), instant) <= distance(indoor.get(nearest), instant)) {
                nearest++;
            }
            if (distance(indoor.get(nearest), instant) <= PAIRING_TOLERANCE.toMillis()) {
                pairs.add(new TemperaturePair(indoor.get(nearest).tempC(), observation.getTempC()));
            }
        }
        return pairs;
    }

    private static long distance(ReadingPoint point, OffsetDateTime instant) {
        return Math.abs(Duration.between(point.ts(), instant).toMillis());
    }

    private List<ReadingPoint> peaksAboveOwnBaseline(List<ReadingPoint> points) {
        double[] levels = points.stream().mapToDouble(ReadingPoint::laeq).toArray();
        double median = new Median().evaluate(levels);
        double mad = new Median().evaluate(
                Arrays.stream(levels).map(level -> Math.abs(level - median)).toArray());

        return points.stream()
                .filter(p -> isPeak(p.laeq() - median, mad))
                .toList();
    }

    private boolean isPeak(double excess, double mad) {
        if (excess < MINIMUM_PEAK_EXCESS_DB) {
            return false;
        }
        return mad == 0 || MAD_CONSISTENCY * excess / mad > ANOMALY_ROBUST_Z;
    }

    private NoiseEpisode episodeOf(List<ReadingPoint> peaks) {
        ReadingPoint loudest = peaks.stream()
                .max(Comparator.comparingDouble(ReadingPoint::laeq))
                .orElseThrow();
        OffsetDateTime from = peaks.getFirst().ts();
        OffsetDateTime to = peaks.getLast().ts();
        return new NoiseEpisode(from, to, (int) Duration.between(from, to).toMinutes() + 1,
                loudest.laeq(), loudest.ts());
    }

    private HourSlot hourSlot(int hour, List<ReadingPoint> points) {
        if (points.isEmpty()) {
            return HourSlot.withoutConclusion(hour, Verdict.NO_DATA, 0);
        }
        if (points.size() < MINIMUM_SAMPLE) {
            return HourSlot.withoutConclusion(hour, Verdict.NOT_ENOUGH_EVIDENCE, points.size());
        }

        double[] levels = points.stream().mapToDouble(ReadingPoint::laeq).toArray();
        double[] occupancy = points.stream()
                .filter(p -> p.occupiedPct() != null)
                .mapToDouble(ReadingPoint::occupiedPct)
                .toArray();
        Double avgOccupied = occupancy.length == 0
                ? null
                : round(Arrays.stream(occupancy).average().orElseThrow());
        return new HourSlot(hour, Verdict.CONCLUSIVE, points.size(),
                round(energyAverage(levels)), round(new Percentile().evaluate(levels, 90)), avgOccupied);
    }

    private static boolean hasNoNoiseData(RoomRanking ranking) {
        return ranking.noiseDistribution() == null;
    }

    private static Double pctOverOf(RoomRanking ranking) {
        return ranking.noiseDistribution() == null ? null : ranking.noiseDistribution().pctOver();
    }

    private static Double p90Of(RoomRanking ranking) {
        return ranking.noiseDistribution() == null ? null : ranking.noiseDistribution().p90();
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private record TemperaturePair(double indoor, double outdoor) {}
}
