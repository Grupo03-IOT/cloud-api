package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.ListApiCredentialsUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.ports.out.ApiCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApiCredentialsUseCaseImpl implements ListApiCredentialsUseCase {

    private final ApiCredentialRepository credentials;

    @Override
    public List<ApiCredential> execute() {
        return credentials.findAll();
    }
}
