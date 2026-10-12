package com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.ApiCredentialEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiCredentialJpaRepository extends JpaRepository<ApiCredentialEntity, UUID> {

    @Query("""
            SELECT c FROM ApiCredentialEntity c
            WHERE c.tokenHash = :tokenHash AND c.deletedAt IS NULL
            """)
    Optional<ApiCredentialEntity> findByTokenHash(String tokenHash);

    @Query("""
            SELECT c FROM ApiCredentialEntity c
            WHERE LOWER(c.code) = LOWER(:code) AND c.deletedAt IS NULL
            """)
    Optional<ApiCredentialEntity> findByCode(String code);

    @Query("""
            SELECT c FROM ApiCredentialEntity c
            WHERE c.deletedAt IS NULL
            ORDER BY LOWER(c.code)
            """)
    List<ApiCredentialEntity> findAllNotDeleted();
}
