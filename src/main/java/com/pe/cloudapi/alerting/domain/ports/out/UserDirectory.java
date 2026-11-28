package com.pe.cloudapi.alerting.domain.ports.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Lo que {@code alerting} necesita saber de las personas: cómo se llama quien
 * reconoció una alerta (US37).
 */
public interface UserDirectory {

    /**
     * @return el nombre completo por id; quien ya no existe no aparece
     */
    Map<UUID, String> namesOf(Collection<UUID> userIds);
}
