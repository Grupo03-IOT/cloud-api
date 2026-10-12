package com.pe.cloudapi.monitoring.application.internal.usecases;

import com.pe.cloudapi.monitoring.application.internal.ports.in.ListGatewaysUseCase;
import com.pe.cloudapi.monitoring.application.internal.results.GatewaySnapshot;
import com.pe.cloudapi.monitoring.domain.ports.out.GatewayRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Listar los Edges con su estado, derivado al consultar de su último latido.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListGatewaysUseCaseImpl implements ListGatewaysUseCase {

    private final GatewayRepository gateways;

    @Override
    public List<GatewaySnapshot> execute() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return gateways.findAll().stream()
                .map(gateway -> new GatewaySnapshot(gateway, gateway.statusAt(now)))
                .toList();
    }
}
