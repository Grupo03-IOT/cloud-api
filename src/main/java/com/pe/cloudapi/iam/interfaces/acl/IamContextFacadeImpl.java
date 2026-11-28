package com.pe.cloudapi.iam.interfaces.acl;

import com.pe.cloudapi.iam.application.internal.ports.in.FindUserNamesUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Delega en los casos de uso de {@code iam}, nunca en sus repositorios.
 */
@Service
@RequiredArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {

    private final FindUserNamesUseCase findUserNames;

    @Override
    public Map<UUID, String> displayNamesOf(Collection<UUID> userIds) {
        return findUserNames.execute(userIds);
    }
}
