package com.pe.cloudapi.alerting.domain.model.valueobjects;

import java.util.UUID;

/**
 * Lo único que este contexto necesita saber de una sala: cómo la llama el
 * dispositivo y de qué tipo es.
 *
 * <p>Vocabulario propio. La sala de {@code monitoring} tiene aforo, planta,
 * superficie y última lectura; aquí nada de eso significa nada, y copiarlo
 * dejaría la capa anticorrupción como decoración.
 *
 * @param code       el código con el que el dispositivo reporta
 * @param roomTypeId nulo si la sala aún no se ha clasificado
 */
public record RoomProfile(String code, UUID roomTypeId) {

    public boolean isClassified() {
        return roomTypeId != null;
    }
}
