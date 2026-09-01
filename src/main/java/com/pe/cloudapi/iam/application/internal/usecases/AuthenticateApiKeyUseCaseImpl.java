package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.AuthenticateApiKeyUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.ports.out.ApiCredentialRepository;
import com.pe.cloudapi.iam.domain.ports.out.ApiKeyHasher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticateApiKeyUseCaseImpl implements AuthenticateApiKeyUseCase {

    private final ApiCredentialRepository credentials;
    private final ApiKeyHasher hasher;

    @Override
    public Optional<ApiCredential> execute(String apiKey) {
        return credentials.findByTokenHash(hasher.hash(apiKey))
                .filter(ApiCredential::isActive);
    }
}
