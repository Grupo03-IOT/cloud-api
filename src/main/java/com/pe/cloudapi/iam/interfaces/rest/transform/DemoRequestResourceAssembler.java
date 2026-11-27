package com.pe.cloudapi.iam.interfaces.rest.transform;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import com.pe.cloudapi.iam.interfaces.rest.resources.DemoRequestReceiptResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.DemoRequestResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.RequestDemoResource;
import org.springframework.stereotype.Component;

@Component
public class DemoRequestResourceAssembler {

    public RequestDemoCommand toCommand(RequestDemoResource resource) {
        return new RequestDemoCommand(resource.fullName(), resource.workEmail(),
                resource.company(), resource.roomCount());
    }

    public DemoRequestReceiptResource toReceipt(DemoRequest request) {
        return new DemoRequestReceiptResource(request.getId(), request.getCreatedAt());
    }

    public DemoRequestResource toResource(DemoRequest request) {
        return new DemoRequestResource(request.getId(), request.getFullName(), request.getWorkEmail(),
                request.getCompany(), request.getRoomCount(), request.getConsentedAt(),
                request.getCreatedAt());
    }
}
