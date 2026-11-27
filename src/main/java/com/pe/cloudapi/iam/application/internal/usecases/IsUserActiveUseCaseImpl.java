package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.IsUserActiveUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IsUserActiveUseCaseImpl implements IsUserActiveUseCase {

    private final UserRepository users;

    @Override
    public boolean execute(UUID userId) {
        return users.findById(userId).map(User::isActive).orElse(false);
    }
}
