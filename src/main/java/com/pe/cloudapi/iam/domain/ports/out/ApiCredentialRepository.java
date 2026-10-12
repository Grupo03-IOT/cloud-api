package com.pe.cloudapi.iam.domain.ports.out;

import com.pe.cloudapi.iam.domain.model.aggregates.ApiCredential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiCredentialRepository {

    ApiCredential save(ApiCredential credential);

    /** El camino de cada petición: se busca por el hash, que está indexado. */
    Optional<ApiCredential> findByTokenHash(String tokenHash);

    boolean existsByCode(String code);

    Optional<ApiCredential> findById(UUID id);

    /** Ordenadas por código, revocadas incluidas. */
    List<ApiCredential> findAll();
}
