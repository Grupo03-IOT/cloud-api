package com.pe.cloudapi.iam.domain.ports.out;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;

import java.util.List;

public interface DemoRequestRepository {

    DemoRequest save(DemoRequest request);

    /** @return todas las solicitudes, la más reciente primero */
    List<DemoRequest> findAllNewestFirst();
}
