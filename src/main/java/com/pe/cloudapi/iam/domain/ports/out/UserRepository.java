package com.pe.cloudapi.iam.domain.ports.out;

import com.pe.cloudapi.iam.domain.model.aggregates.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    /** @param email en minúsculas; el índice único es sobre {@code LOWER(email)} */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * @return el nombre de cada cuenta no borrada entre las pedidas
     */
    Map<UUID, String> findDisplayNames(Collection<UUID> ids);

    /** Ordenadas por correo. */
    List<User> findAll();
}
