package com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.ApiCredentialEntity;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class ApiCredentialMapper {

    public ApiCredential toDomain(ApiCredentialEntity entity) {
        if (entity == null) {
            return null;
        }
        return ApiCredential.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .tokenHash(entity.getTokenHash())
                .active(Boolean.TRUE.equals(entity.getActive()))
                .scopes(entity.getScopes().stream().map(Scope::fromCode)
                        .collect(Collectors.toCollection(HashSet::new)))
                .build();
    }

    public ApiCredentialEntity applyTo(ApiCredentialEntity entity, ApiCredential domain) {
        entity.setCode(domain.getCode());
        entity.setTokenHash(domain.getTokenHash());
        entity.setActive(domain.isActive());
        entity.setScopes(domain.getScopes().stream().map(Scope::toCode)
                .collect(Collectors.toCollection(HashSet::new)));
        return entity;
    }
}
