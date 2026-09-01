package com.pe.cloudapi.iam.domain.model.aggregates;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

/**
 * Una persona con acceso al sistema.
 *
 * <p>{@code passwordHash} es el hash, nunca la contraseña.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class User {

    private final UUID id;
    private final String email;
    @Setter private String passwordHash;
    @Setter private String displayName;
    @Setter private boolean active;
    @Setter private Set<Role> roles;

    public User(String email, String passwordHash, String displayName, Set<Role> roles) {
        this.id = null;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.roles = roles;
        this.active = true;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_ACCOUNT_DISABLED} si la cuenta está desactivada
     */
    public void ensureCanSignIn() {
        if (!active) {
            throw IamError.ACCOUNT_DISABLED.with();
        }
    }
}
