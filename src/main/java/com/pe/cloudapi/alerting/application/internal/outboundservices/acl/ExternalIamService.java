package com.pe.cloudapi.alerting.application.internal.outboundservices.acl;

import com.pe.cloudapi.alerting.domain.ports.out.UserDirectory;
import com.pe.cloudapi.iam.interfaces.acl.IamContextFacade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Capa anticorrupción entre {@code alerting} e {@code iam}.
 */
@Service
@RequiredArgsConstructor
public class ExternalIamService implements UserDirectory {

    private final IamContextFacade iam;

    @Override
    public Map<UUID, String> namesOf(Collection<UUID> userIds) {
        return iam.displayNamesOf(userIds);
    }
}
