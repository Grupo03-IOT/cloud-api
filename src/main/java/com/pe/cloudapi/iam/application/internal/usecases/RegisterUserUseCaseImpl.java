package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.RegisterUserUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.commands.RegisterUserCommand;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.ports.out.PasswordHasher;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;

    @Override
    public User execute(RegisterUserCommand command) {
        if (users.existsByEmail(command.email())) {
            throw IamError.EMAIL_ALREADY_USED.with(command.email());
        }
        return users.save(new User(
                command.email(),
                hasher.hash(command.plainPassword()),
                command.displayName(),
                command.roles()));
    }
}
