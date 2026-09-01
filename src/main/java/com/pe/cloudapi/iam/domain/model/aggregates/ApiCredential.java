package com.pe.cloudapi.iam.domain.model.aggregates;

import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

/**
 * La credencial de una máquina: el Edge, y lo que venga después.
 *
 * <p>{@code tokenHash} es el hash de la clave. La clave en claro solo existe al
 * emitirla: si se pierde, se emite otra.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApiCredential {

    private final UUID id;
    private final String code;
    private final String tokenHash;
    @Setter private boolean active;
    @Setter private Set<Scope> scopes;

    public ApiCredential(String code, String tokenHash, Set<Scope> scopes) {
        this.id = null;
        this.code = code;
        this.tokenHash = tokenHash;
        this.scopes = scopes;
        this.active = true;
    }

    /**
     * @throws com.pe.cloudapi.shared.domain.model.errors.DomainException
     *         {@code IAM_CREDENTIAL_REVOKED} si está desactivada
     */
    public void ensureUsable() {
        if (!active) {
            throw IamError.CREDENTIAL_REVOKED.with(code);
        }
    }
}
