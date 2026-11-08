package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.SetUserActiveUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SetUserActiveUseCaseImpl implements SetUserActiveUseCase {

    private final UserRepository users;

    @Override
    public User execute(UUID userId, boolean active) {
        User user = users.findById(userId).orElseThrow(() -> IamError.USER_NOT_FOUND.with(userId));
        user.changeActive(active);
        return users.save(user);
    }
}
