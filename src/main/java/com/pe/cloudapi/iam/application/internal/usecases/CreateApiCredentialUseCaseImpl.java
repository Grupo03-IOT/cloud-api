package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.CreateApiCredentialUseCase;
import com.pe.cloudapi.iam.application.internal.results.CreatedCredential;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.iam.domain.ports.out.ApiCredentialRepository;
import com.pe.cloudapi.iam.domain.ports.out.ApiKeyHasher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateApiCredentialUseCaseImpl implements CreateApiCredentialUseCase {

    private final ApiCredentialRepository credentials;
    private final ApiKeyHasher hasher;

    @Override
    public CreatedCredential execute(String code, Set<Scope> scopes) {
        if (credentials.existsByCode(code)) {
            throw IamError.CREDENTIAL_CODE_ALREADY_USED.with(code);
        }
        String apiKey = hasher.generate();
        ApiCredential saved = credentials.save(
                new ApiCredential(code, hasher.hash(apiKey), scopes));

        return new CreatedCredential(saved, apiKey);
    }
}
