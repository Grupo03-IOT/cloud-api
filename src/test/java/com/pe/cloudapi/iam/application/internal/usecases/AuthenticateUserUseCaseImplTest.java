package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.errors.IamError;
import com.pe.cloudapi.iam.domain.model.events.UserActiveChangedEvent;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.domain.ports.out.PasswordHasher;
import com.pe.cloudapi.iam.domain.ports.out.TokenIssuer;
import com.pe.cloudapi.iam.domain.ports.out.UserRepository;
import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import com.pe.cloudapi.shared.domain.model.errors.ErrorKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("TS7 / US10 - sign in and account status")
class AuthenticateUserUseCaseImplTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordHasher hasher = mock(PasswordHasher.class);
    private final TokenIssuer tokens = mock(TokenIssuer.class);
    private final AuthenticateUserUseCaseImpl signIn = new AuthenticateUserUseCaseImpl(users, hasher, tokens);

    @Test
    @DisplayName("an unknown email and a wrong password give the same unauthenticated error")
    void sameErrorForEmailAndPassword() {
        when(users.findByEmail("ghost@comfort.pe")).thenReturn(Optional.empty());
        when(users.findByEmail("ana@comfort.pe")).thenReturn(Optional.of(member(true)));
        when(hasher.matches("wrong", "hash")).thenReturn(false);

        DomainException unknown = catchDomain(() -> signIn.execute("ghost@comfort.pe", "whatever"));
        DomainException wrong = catchDomain(() -> signIn.execute("ana@comfort.pe", "wrong"));

        assertThat(unknown.getError()).isEqualTo(IamError.INVALID_CREDENTIALS);
        assertThat(wrong.getError()).isEqualTo(IamError.INVALID_CREDENTIALS);
        assertThat(unknown.getMessage()).isEqualTo(wrong.getMessage());
        assertThat(unknown.getKind()).isEqualTo(ErrorKind.UNAUTHENTICATED);
        verifyNoInteractions(tokens);
    }

    @Test
    @DisplayName("a deactivated account cannot sign in, even with the right password")
    void disabledCannotSignIn() {
        when(users.findByEmail("ana@comfort.pe")).thenReturn(Optional.of(member(false)));
        when(hasher.matches("right", "hash")).thenReturn(true);

        assertThat(catchDomain(() -> signIn.execute("ana@comfort.pe", "right")).getError())
                .isEqualTo(IamError.ACCOUNT_DISABLED);
        verifyNoInteractions(tokens);
    }

    @Test
    @DisplayName("a deactivated account with a wrong password does not reveal it is deactivated")
    void disabledWithWrongPassword() {
        when(users.findByEmail("ana@comfort.pe")).thenReturn(Optional.of(member(false)));
        when(hasher.matches("wrong", "hash")).thenReturn(false);

        assertThat(catchDomain(() -> signIn.execute("ana@comfort.pe", "wrong")).getError())
                .isEqualTo(IamError.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("an active account signs in")
    void activeSignsIn() {
        User user = member(true);
        when(users.findByEmail("ana@comfort.pe")).thenReturn(Optional.of(user));
        when(hasher.matches("right", "hash")).thenReturn(true);
        when(tokens.issueFor(user)).thenReturn(new IssuedToken("token", 3600));

        assertThat(signIn.execute("ana@comfort.pe", "right").value()).isEqualTo("token");
    }

    @Test
    @DisplayName("deactivating an account publishes the change so live tokens stop working")
    void deactivationPublishesEvent() {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        SetUserActiveUseCaseImpl setActive = new SetUserActiveUseCaseImpl(users, events);
        User user = member(true);
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(call -> call.getArgument(0));

        setActive.execute(user.getId(), false);

        verify(events).publishEvent(new UserActiveChangedEvent(user.getId(), false));
    }

    @Test
    @DisplayName("an unknown or deactivated account is not active")
    void isActive() {
        IsUserActiveUseCaseImpl isActive = new IsUserActiveUseCaseImpl(users);
        User active = member(true);
        User disabled = member(false);
        when(users.findById(active.getId())).thenReturn(Optional.of(active));
        when(users.findById(disabled.getId())).thenReturn(Optional.of(disabled));

        assertThat(isActive.execute(active.getId())).isTrue();
        assertThat(isActive.execute(disabled.getId())).isFalse();
        assertThat(isActive.execute(UUID.randomUUID())).isFalse();
    }

    private static User member(boolean active) {
        return User.builder().id(UUID.randomUUID()).email("ana@comfort.pe").passwordHash("hash")
                .displayName("Ana").active(active).roles(Set.of(Role.MEMBER)).build();
    }

    private static DomainException catchDomain(Runnable call) {
        Throwable thrown = catchThrowable(call::run);
        assertThat(thrown).isInstanceOf(DomainException.class);
        return (DomainException) thrown;
    }
}
