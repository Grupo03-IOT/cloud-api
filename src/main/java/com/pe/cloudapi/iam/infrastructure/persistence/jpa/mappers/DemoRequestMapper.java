package com.pe.cloudapi.iam.infrastructure.persistence.jpa.mappers;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities.DemoRequestEntity;
import org.springframework.stereotype.Component;

/** Traduce entre {@code DemoRequest} y su entidad JPA. */
@Component
public class DemoRequestMapper {

    public DemoRequest toDomain(DemoRequestEntity entity) {
        if (entity == null) return null;
        return DemoRequest.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .workEmail(entity.getWorkEmail())
                .company(entity.getCompany())
                .roomCount(entity.getRoomCount())
                .consentedAt(entity.getConsentedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public DemoRequestEntity toEntity(DemoRequest domain) {
        DemoRequestEntity entity = new DemoRequestEntity();
        entity.setFullName(domain.getFullName());
        entity.setWorkEmail(domain.getWorkEmail());
        entity.setCompany(domain.getCompany());
        entity.setRoomCount(domain.getRoomCount());
        entity.setConsentedAt(domain.getConsentedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
