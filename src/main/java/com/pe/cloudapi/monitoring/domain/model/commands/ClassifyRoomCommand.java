package com.pe.cloudapi.monitoring.domain.model.commands;

import java.util.UUID;

/**
 * Asigna un tipo a una sala. Hasta que ocurre, la sala no tiene umbrales
 * aplicables porque los umbrales cuelgan del tipo.
 */
public record ClassifyRoomCommand(UUID roomId, UUID roomTypeId) {}
