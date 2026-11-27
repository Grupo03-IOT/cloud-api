package com.pe.cloudapi.iam.infrastructure.persistence.jpa.repositories;

import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.DemoRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DemoRequestJpaRepository extends JpaRepository<DemoRequestEntity, UUID> {

    List<DemoRequestEntity> findAllByOrderByCreatedAtDesc();
}
