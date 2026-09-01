package com.pe.cloudapi.iam.interfaces.rest.transform;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.commands.RegisterUserCommand;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.interfaces.rest.resources.RegisterUserResource;
import com.pe.cloudapi.iam.interfaces.rest.resources.UserResource;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserResourceAssembler {

    /**
     * El registro público siempre crea un {@code MEMBER}. El rol no se acepta
     * del cliente: quien se registra elegiría {@code ADMIN}.
     */
    public RegisterUserCommand toCommand(RegisterUserResource resource) {
        return new RegisterUserCommand(resource.email(), resource.password(),
                resource.displayName(), Set.of(Role.MEMBER));
    }

    public UserResource toResource(User user) {
        return new UserResource(user.getId(), user.getEmail(), user.getDisplayName(),
                user.isActive(), user.getRoles().stream().map(Role::toCode).sorted().toList());
    }
}
