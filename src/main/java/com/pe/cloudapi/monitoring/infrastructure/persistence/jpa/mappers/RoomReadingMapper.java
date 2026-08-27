package com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomReading;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.AcousticMetrics;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThermalComfort;
import com.pe.cloudapi.monitoring.infrastructure.persistence.jpa.entities.RoomReadingEntity;

import org.springframework.stereotype.Component;

/**
 * Traduce entre el agregado {@code RoomReading} y su entidad JPA, agrupando y
 * desagrupando los value objects en las columnas planas de la tabla.
 */
@Component
public class RoomReadingMapper {

    public RoomReading toDomain(RoomReadingEntity e) {
        if (e == null) return null;
        return RoomReading.builder()
                .id(e.getId())
                .roomId(e.getRoomId())
                .ts(e.getTs())
                .periodS(e.getPeriodS() == null ? 60 : e.getPeriodS())
                .acoustic(new AcousticMetrics(e.getLaeq(), e.getL10(), e.getL50(),
                        e.getL90(), e.getLmax(), e.getLmin()))
                .climate(new Climate(e.getTempC(), e.getRhPct()))
                .comfort(new ThermalComfort(e.getPmv(), e.getPpd(), e.getThermalVerdict()))
                .occupancy(new Occupancy(e.getOccupiedPct(), e.getTransitions()))
                .quality(new DataQuality(e.getBatches(), e.getExpected()))
                .receivedAt(e.getReceivedAt())
                .build();
    }

    public RoomReadingEntity toEntity(RoomReading domain) {
        return applyTo(new RoomReadingEntity(), domain);
    }

    public RoomReadingEntity applyTo(RoomReadingEntity e, RoomReading d) {
        e.setRoomId(d.getRoomId());
        e.setTs(d.getTs());
        e.setPeriodS(d.getPeriodS());

        AcousticMetrics a = d.getAcoustic();
        e.setLaeq(a.laeq());
        e.setL10(a.l10());
        e.setL50(a.l50());
        e.setL90(a.l90());
        e.setLmax(a.lmax());
        e.setLmin(a.lmin());

        e.setTempC(d.getClimate().tempC());
        e.setRhPct(d.getClimate().rhPct());

        e.setPmv(d.getComfort().pmv());
        e.setPpd(d.getComfort().ppd());
        e.setThermalVerdict(d.getComfort().verdict());

        Occupancy o = d.getOccupancy();
        e.setOccupiedPct(o.occupiedPct() == null ? 0f : o.occupiedPct());
        e.setTransitions(o.transitions() == null ? 0 : o.transitions());

        e.setBatches(d.getQuality().batches());
        e.setExpected(d.getQuality().expected());
        e.setReceivedAt(d.getReceivedAt());
        return e;
    }
}
