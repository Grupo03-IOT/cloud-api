package com.pe.cloudapi.monitoring.interfaces.rest.resources;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Un minuto de una sala, tal como lo sube el Edge.
 *
 * <p>El JSON viaja en snake_case; el mapeo a camelCase lo hace Jackson por la
 * property-naming-strategy configurada en {@code application.yaml}, así que no
 * hacen falta anotaciones {@code @JsonProperty}.
 *
 * <p>Los valores acústicos y de confort llegan <strong>ya calculados</strong>
 * desde el Edge. Aquí no se recalcula nada.
 */
@Schema(description = "Lectura agregada de un minuto para una sala")
public record ReadingResource(

        @NotBlank
        @Schema(description = "Código de la sala configurado en el firmware",
                example = "sala-01")
        String roomId,

        @NotNull
        @Schema(description = "Inicio del minuto que resume la lectura, en UTC",
                example = "2026-08-26T11:59:00Z")
        OffsetDateTime ts,

        @NotNull(message = "MONITORING_READING_PERIOD_REQUIRED")
        @Positive
        @Schema(description = "Duración del periodo en segundos. Obligatorio: entra "
                + "en el cálculo de los agregados, así que asumirlo falsearía las "
                + "medias energéticas sin que nadie lo note",
                example = "60")
        Integer periodS,

        @Valid Device device,
        @Valid Acoustic acoustic,
        @Valid Climate climate,
        @Valid Comfort comfort,
        @Valid Occupancy occupancy,
        @Valid Quality quality

) {

    /**
     * Estado del módulo que reporta por la sala. Puede venir nulo si el Edge
     * todavía no sabe de ningún dispositivo para ella.
     */
    @Schema(description = "Estado del módulo ESP32, reportado por el Edge")
    public record Device(
            @Schema(example = "esp32-sala-01") String code,
            String fwVersion,
            @Schema(description = "Instante del último lote recibido por el Edge")
            OffsetDateTime lastSeen,
            @Schema(description = "Secuencia del último lote") Long lastSeq,
            @Schema(description = "Lotes perdidos acumulados, contados por el Edge")
            Long lostBatches
    ) {}

    /** Métricas acústicas ISO 1996, ponderadas A y en dBA. */
    @Schema(description = "Niveles de presión sonora del periodo, en dBA")
    public record Acoustic(
            @Schema(description = "Nivel continuo equivalente") Float laeq,
            @Schema(description = "Superado el 10% del tiempo: picos intrusivos") Float l10,
            Float l50,
            @Schema(description = "Superado el 90% del tiempo: ruido de fondo") Float l90,
            Float lmax,
            Float lmin
    ) {}

    @Schema(description = "Lecturas ambientales")
    public record Climate(Float tempC, Float rhPct) {}

    /** Confort térmico ISO 7730. */
    @Schema(description = "Confort térmico calculado en el Edge")
    public record Comfort(
            @Schema(description = "Predicted Mean Vote, -3..+3") Float pmv,
            @Schema(description = "Predicted Percentage of Dissatisfied") Float ppd,
            @Schema(example = "neutral") String verdict,
            @Schema(description = "Asunciones del modelo: tr, vel, met, clo")
            Map<String, Object> assumptions
    ) {}

    @Schema(description = "Ocupación derivada del sensor de presencia")
    public record Occupancy(Float occupiedPct, Integer transitions) {}

    /**
     * Cuántos lotes de 10 s respaldan el minuto. Si {@code batches} es menor que
     * {@code expected}, hubo pérdida entre el dispositivo y el Edge.
     */
    @Schema(description = "Calidad del dato")
    public record Quality(Integer batches, Integer expected) {}
}
