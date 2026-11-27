package com.pe.cloudapi.iam.application.internal.ports.in;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;

public interface RequestDemoUseCase {

    DemoRequest execute(RequestDemoCommand command);
}
