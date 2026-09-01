package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateUserUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.ports.out.PasswordHasher;
import com.pe.cloudapi.iam.domain.ports.out.TokenIssuer;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;

    @Override
    public IssuedToken execute(String email, String plainPassword) {
        User user = users.findByEmail(email)
                .orElseThrow(IamError.INVALID_CREDENTIALS::with);

        if (!hasher.matches(plainPassword, user.getPasswordHash())) {
            throw IamError.INVALID_CREDENTIALS.with();
        }
        user.ensureCanSignIn();
        return tokens.issueFor(user);
    }
}
