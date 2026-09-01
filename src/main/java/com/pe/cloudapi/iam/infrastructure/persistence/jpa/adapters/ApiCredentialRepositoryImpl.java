package com.pe.cloudapi.iam.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.ports.out.ApiCredentialRepository;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.ApiCredentialEntity;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers.ApiCredentialMapper;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories.ApiCredentialJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiCredentialRepositoryImpl implements ApiCredentialRepository {

    private final ApiCredentialJpaRepository jpa;
    private final ApiCredentialMapper mapper;

    @Override
    public ApiCredential save(ApiCredential credential) {
        ApiCredentialEntity entity = credential.getId() == null
                ? new ApiCredentialEntity()
                : jpa.findById(credential.getId()).orElseGet(ApiCredentialEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, credential)));
    }

    @Override
    public Optional<ApiCredential> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.findByCode(code).isPresent();
    }
}
