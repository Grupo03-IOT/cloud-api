package com.pe.cloudapi.iam.domain.model.aggregates;

import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User aggregate")
class UserTest {

    @Test
    @DisplayName("is created active with its roles and can sign in")
    void createdActive() {
        User user = new User("admin@comfort.pe", "hash", "Admin", Set.of(Role.ADMIN));

        assertThat(user.isActive()).isTrue();
        assertThat(user.hasRole(Role.ADMIN)).isTrue();
        assertThat(user.hasRole(Role.MEMBER)).isFalse();
        assertThatCode(user::ensureCanSignIn).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a disabled account cannot sign in")
    void disabledCannotSignIn() {
        User user = new User("member@comfort.pe", "hash", "Member", Set.of(Role.MEMBER));
        user.setActive(false);

        assertThatThrownBy(user::ensureCanSignIn)
                .isInstanceOf(DomainException.class)
                .extracting("code").isEqualTo("IAM_ACCOUNT_DISABLED");
    }
}
