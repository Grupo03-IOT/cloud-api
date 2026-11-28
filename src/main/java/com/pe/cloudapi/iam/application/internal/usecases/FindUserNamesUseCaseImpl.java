package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.FindUserNamesUseCase;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserNamesUseCaseImpl implements FindUserNamesUseCase {

    private final UserRepository users;

    @Override
    public Map<UUID, String> execute(Collection<UUID> userIds) {
        return userIds.isEmpty() ? Map.of() : users.findDisplayNames(userIds);
    }
}
