package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.RevokeApiCredentialUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.ports.out.ApiCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Revocar una credencial. Se desactiva, no se borra: queda el rastro de que existió.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RevokeApiCredentialUseCaseImpl implements RevokeApiCredentialUseCase {

    private final ApiCredentialRepository credentials;

    @Override
    public void execute(UUID credentialId) {
        ApiCredential credential = credentials.findById(credentialId)
                .orElseThrow(() -> IamError.CREDENTIAL_NOT_FOUND.with(credentialId));
        credential.revoke();
        credentials.save(credential);
    }
}
