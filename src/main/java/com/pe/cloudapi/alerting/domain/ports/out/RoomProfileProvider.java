package com.pe.cloudapi.alerting.domain.ports.out;

import com.pe.cloudapi.alerting.domain.model.valueobjects.RoomProfile;

import java.util.List;

/**
 * De dónde salen las salas cuyos umbrales hay que resolver.
 *
 * <p>Puerto de salida: {@code alerting} declara que necesita saber qué salas
 * hay y de qué tipo es cada una, sin saber que eso lo responde
 * {@code monitoring}.
 */
public interface RoomProfileProvider {

    List<RoomProfile> rooms();
}
