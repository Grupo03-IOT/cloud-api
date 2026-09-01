package com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.UserEntity;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .displayName(entity.getDisplayName())
                .active(Boolean.TRUE.equals(entity.getActive()))
                .roles(entity.getRoles().stream().map(Role::fromCode)
                        .collect(Collectors.toCollection(HashSet::new)))
                .build();
    }

    public UserEntity applyTo(UserEntity entity, User domain) {
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setDisplayName(domain.getDisplayName());
        entity.setActive(domain.isActive());
        entity.setRoles(domain.getRoles().stream().map(Role::toCode)
                .collect(Collectors.toCollection(HashSet::new)));
        return entity;
    }
}
