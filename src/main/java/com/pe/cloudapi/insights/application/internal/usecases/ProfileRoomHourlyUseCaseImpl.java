package com.pe.cloudapi.insights.application.internal.usecases;

import com.pe.cloudapi.insights.application.internal.ports.in.ProfileRoomHourlyUseCase;
import com.pe.cloudapi.insights.domain.model.queries.ProfileRoomHourlyQuery;
import com.pe.cloudapi.insights.domain.model.valueobjects.AnalyzedRoom;
import com.pe.cloudapi.insights.domain.model.valueobjects.HourlyProfile;
import com.pe.cloudapi.insights.domain.model.valueobjects.PeriodWindow;
import com.pe.cloudapi.insights.domain.ports.out.ReadingSeriesProvider;
import com.pe.cloudapi.insights.domain.ports.out.RoomCatalog;
import com.pe.cloudapi.insights.domain.services.ComfortAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Perfil hora a hora de una sala, en la zona horaria de su local.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileRoomHourlyUseCaseImpl implements ProfileRoomHourlyUseCase {

    private final RoomCatalog rooms;
    private final ReadingSeriesProvider readings;
    private final ComfortAnalyticsService analytics;

    @Override
    public HourlyProfile execute(ProfileRoomHourlyQuery query) {
        AnalyzedRoom room = rooms.room(query.roomId());
        PeriodWindow window = query.period().in(room.zone());
        return analytics.hourlyProfile(room.id(), window,
                readings.seriesOf(room.id(), window.from(), window.to()));
    }
}
