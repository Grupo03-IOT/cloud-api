package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.application.internal.results.IngestResult;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;
import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.AcousticMetrics;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThermalComfort;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.IngestResultResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.ReadingResource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce entre los recursos REST de la ingesta y los objetos de entrada y
 * salida de la capa de aplicación.
 *
 * <p>Vive en la capa de transporte a propósito: es la única que conoce ambos
 * lados. La aplicación no sabe que existe JSON, y por eso no puede ser ella
 * quien traduzca.
 */
@Component
public class ReadingResourceAssembler {

    /** Convierte el lote recibido en el comando que espera el caso de uso. */
    public IngestReadingsCommand toCommand(List<ReadingResource> resources) {
        return new IngestReadingsCommand(resources.stream().map(this::toReading).toList());
    }

    private IngestReadingsCommand.Reading toReading(ReadingResource resource) {
        return new IngestReadingsCommand.Reading(
                resource.roomId(),
                resource.ts(),
                resource.periodS(),
                toDevice(resource.device()),
                toAcoustic(resource.acoustic()),
                toClimate(resource.climate()),
                toComfort(resource.comfort()),
                toOccupancy(resource.occupancy()),
                toQuality(resource.quality()));
    }

    /** Convierte el resultado del caso de uso al recurso de respuesta. */
    public IngestResultResource toResource(IngestResult result) {
        return new IngestResultResource(
                result.accepted(), result.inserted(), result.updated());
    }

    private IngestReadingsCommand.Reading.Device toDevice(ReadingResource.Device device) {
        if (device == null) return null;
        return new IngestReadingsCommand.Reading.Device(
                device.code(), device.fwVersion(), device.lastSeen(),
                device.lastSeq(), device.lostBatches());
    }

    private AcousticMetrics toAcoustic(ReadingResource.Acoustic a) {
        if (a == null) return AcousticMetrics.empty();
        return new AcousticMetrics(a.laeq(), a.l10(), a.l50(), a.l90(), a.lmax(), a.lmin());
    }

    private Climate toClimate(ReadingResource.Climate c) {
        if (c == null) return Climate.empty();
        return new Climate(c.tempC(), c.rhPct());
    }

    private ThermalComfort toComfort(ReadingResource.Comfort c) {
        if (c == null) return ThermalComfort.empty();
        return new ThermalComfort(c.pmv(), c.ppd(), c.verdict());
    }

    private Occupancy toOccupancy(ReadingResource.Occupancy o) {
        if (o == null) return Occupancy.vacant();
        return new Occupancy(
                o.occupiedPct() == null ? 0f : o.occupiedPct(),
                o.transitions() == null ? 0 : o.transitions());
    }

    private DataQuality toQuality(ReadingResource.Quality q) {
        if (q == null) return DataQuality.unknown();
        return new DataQuality(q.batches(), q.expected());
    }
}
