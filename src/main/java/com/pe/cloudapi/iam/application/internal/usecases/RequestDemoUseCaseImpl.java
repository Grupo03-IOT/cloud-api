package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.RequestDemoUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import com.pe.cloudapi.iam.domain.ports.out.DemoRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Registrar una solicitud de demostración (US06). */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestDemoUseCaseImpl implements RequestDemoUseCase {

    private final DemoRequestRepository requests;

    @Override
    @Transactional
    public DemoRequest execute(RequestDemoCommand command) {
        DemoRequest saved = requests.save(new DemoRequest(command, OffsetDateTime.now(ZoneOffset.UTC)));
        // Sin nombre ni correo: son datos personales y no van al registro.
        log.info("demo.requested id={} roomCount={}", saved.getId(), saved.getRoomCount());
        return saved;
    }
}
