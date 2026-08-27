package com.pe.cloudapi.monitoring.domain.model.commands;

import java.util.UUID;

/**
 * Da de alta un módulo ESP32 y lo asocia a una sala.
 */
public record RegisterDeviceCommand(String code, UUID roomId) {}
