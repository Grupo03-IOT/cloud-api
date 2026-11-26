package com.pe.cloudapi.monitoring.interfaces.rest.transform;

import com.pe.cloudapi.monitoring.application.internal.results.DiscomfortSummary;
import com.pe.cloudapi.monitoring.application.internal.results.RoomDiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.aggregates.DiscomfortReport;
import com.pe.cloudapi.monitoring.domain.model.commands.FileDiscomfortReportCommand;
import com.pe.cloudapi.monitoring.domain.model.queries.ListDiscomfortReportsQuery;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DiscomfortCategory;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ReadingAtReport;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DiscomfortReportResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.DiscomfortSummaryResource;
import com.pe.cloudapi.monitoring.interfaces.rest.resources.FileDiscomfortReportResource;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Traduce entre los recursos REST de reportes de incomodidad y el dominio.
 */
@Component
public class DiscomfortReportResourceAssembler {

    public FileDiscomfortReportCommand toCommand(UUID userId, FileDiscomfortReportResource resource) {
        return new FileDiscomfortReportCommand(resource.roomId(), userId,
                DiscomfortCategory.fromCode(resource.category()), resource.comment());
    }

    public ListDiscomfortReportsQuery toQuery(UUID roomId, String category,
                                              OffsetDateTime from, OffsetDateTime to) {
        return new ListDiscomfortReportsQuery(roomId,
                category == null || category.isBlank() ? null : DiscomfortCategory.fromCode(category),
                from, to);
    }

    public DiscomfortReportResource toResource(RoomDiscomfortReport result) {
        DiscomfortReport report = result.report();
        ReadingAtReport reading = report.getReading();
        return new DiscomfortReportResource(
                report.getId(),
                report.getRoomId(),
                result.roomCode(),
                report.getUserId(),
                report.getCategory().name(),
                report.getComment(),
                report.getCreatedAt(),
                reading == null ? null : new DiscomfortReportResource.Reading(
                        reading.ts(), reading.laeq(), reading.tempC(), reading.rhPct(), reading.occupiedPct()));
    }

    public DiscomfortSummaryResource toResource(DiscomfortSummary summary) {
        return new DiscomfortSummaryResource(summary.roomId(), summary.roomCode(),
                summary.noise(), summary.warm(), summary.cold(), summary.total());
    }
}
