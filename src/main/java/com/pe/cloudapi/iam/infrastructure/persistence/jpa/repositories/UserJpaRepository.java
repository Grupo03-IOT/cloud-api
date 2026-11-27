package com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    @Query("""
            SELECT u FROM UserEntity u
            WHERE LOWER(u.email) = LOWER(:email) AND u.deletedAt IS NULL
            """)
    Optional<UserEntity> findByEmail(String email);

    @Query("""
            SELECT u FROM UserEntity u
            WHERE u.deletedAt IS NULL
            ORDER BY LOWER(u.email)
            """)
    List<UserEntity> findAllActive();

    /** Solo id y nombre: cargar la entidad traería los roles con una consulta por usuario. */
    @Query("""
            SELECT u.id AS id, u.displayName AS displayName FROM UserEntity u
            WHERE u.id IN :ids AND u.deletedAt IS NULL
            """)
    List<DisplayNameView> findDisplayNames(@Param("ids") Collection<UUID> ids);

    interface DisplayNameView {
        UUID getId();

        String getDisplayName();
    }
}
