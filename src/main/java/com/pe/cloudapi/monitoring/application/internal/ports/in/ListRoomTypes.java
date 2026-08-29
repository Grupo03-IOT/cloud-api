package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.domain.model.aggregates.RoomType;
import com.pe.cloudapi.monitoring.domain.model.queries.ListRoomTypesQuery;

import java.util.List;

/**
 * Puerto de entrada: listar los tipos de sala de un local.
 */
public interface ListRoomTypes {

    List<RoomType> execute(ListRoomTypesQuery query);
}
