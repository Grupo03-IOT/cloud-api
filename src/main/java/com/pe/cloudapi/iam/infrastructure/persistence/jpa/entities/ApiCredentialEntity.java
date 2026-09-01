package com.pe.cloudapi.iam.infrastructure.persistence.jpa.entities;

import com.pe.cloudapi.shared.infrastructure.persistence.jpa.entities.AuditableModel;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "api_credential", schema = "iam")
@Getter
@Setter
@NoArgsConstructor
public class ApiCredentialEntity extends AuditableModel {

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_credential_scope", schema = "iam",
            joinColumns = @JoinColumn(name = "credential_id"))
    @Column(name = "scope", nullable = false, length = 48)
    private Set<String> scopes = new HashSet<>();
}
