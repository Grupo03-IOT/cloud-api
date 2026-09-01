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
@Table(name = "user_account", schema = "iam")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends AuditableModel {

    @Column(name = "email", nullable = false, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * Ansioso a propósito: los roles hacen falta siempre que se carga un
     * usuario —para autenticar y para autorizar—, así que diferirlos solo daría
     * una consulta más.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", schema = "iam",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 32)
    private Set<String> roles = new HashSet<>();
}
