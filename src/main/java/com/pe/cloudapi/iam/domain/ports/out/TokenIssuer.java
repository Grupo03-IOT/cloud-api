package com.pe.cloudapi.iam.domain.ports.out;

import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.valueobjects.IssuedToken;

/**
 * Emitir la credencial con la que alguien demuestra quién es en las peticiones
 * siguientes.
 */
public interface TokenIssuer {

    IssuedToken issueFor(User user);
}
