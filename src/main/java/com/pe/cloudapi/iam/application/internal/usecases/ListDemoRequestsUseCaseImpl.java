package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.ListDemoRequestsUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.ports.out.DemoRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListDemoRequestsUseCaseImpl implements ListDemoRequestsUseCase {

    private final DemoRequestRepository requests;

    @Override
    @Transactional(readOnly = true)
    public List<DemoRequest> execute() {
        return requests.findAllNewestFirst();
    }
}
