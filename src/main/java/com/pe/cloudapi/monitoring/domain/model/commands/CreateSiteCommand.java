package com.pe.cloudapi.monitoring.domain.model.commands;

/**
 * Da de alta un local.
 */
public record CreateSiteCommand(
        String code,
        String name,
        String address,
        String timezone
) {}
