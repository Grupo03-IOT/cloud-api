package com.pe.cloudapi.iam.infrastructure.authentication;

import com.pe.cloudapi.iam.domain.ports.out.PasswordHasher;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCrypt: unos 100 ms por comprobación, y esa lentitud es la defensa.
 */
@Component
@RequiredArgsConstructor
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder;

    @Override
    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String hash) {
        return encoder.matches(plainPassword, hash);
    }
}
