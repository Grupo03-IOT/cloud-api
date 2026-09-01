package com.pe.cloudapi.iam.infrastructure.persistence.jpa.adapters;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.UserEntity;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers.UserMapper;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories.UserJpaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpa;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        UserEntity entity = user.getId() == null
                ? new UserEntity()
                : jpa.findById(user.getId()).orElseGet(UserEntity::new);
        return mapper.toDomain(jpa.save(mapper.applyTo(entity, user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.findByEmail(email).isPresent();
    }
}
