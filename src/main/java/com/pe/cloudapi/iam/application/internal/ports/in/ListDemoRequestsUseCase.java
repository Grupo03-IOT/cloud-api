package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;

import java.util.List;

public interface ListDemoRequestsUseCase {

    List<DemoRequest> execute();
}
