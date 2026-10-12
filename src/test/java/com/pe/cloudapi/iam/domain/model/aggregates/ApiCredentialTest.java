package com.pe.cloudapi.iam.domain.model.aggregates;

import com.pe.cloudapi.iam.domain.model.valueobjects.Scope;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("API credential aggregate")
class ApiCredentialTest {

    @Test
    @DisplayName("a revoked credential can no longer be used, and revoking twice is not an error")
    void revoke() {
        ApiCredential credential = new ApiCredential("edge-lima-centro", "hash", Set.of(Scope.READINGS_WRITE));

        credential.revoke();
        credential.revoke();

        assertThat(credential.isActive()).isFalse();
        assertThatThrownBy(credential::ensureUsable)
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("IAM_CREDENTIAL_REVOKED");
    }
}
