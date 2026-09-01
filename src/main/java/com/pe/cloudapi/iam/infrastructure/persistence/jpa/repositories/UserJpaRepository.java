package com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    @Query("""
            SELECT u FROM UserEntity u
            WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL
            """)
    Optional<UserEntity> findByEmail(String email);
}
