package com.pe.cloudapi.iam.interfaces.acl;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * La puerta pública de {@code iam} hacia los demás contextos.
 *
 * <p>Este contrato es toda la superficie: lo que no esté declarado aquí, no se expone.
 */
public interface IamContextFacade {

    /**
     * El nombre completo de cada cuenta pedida, en una sola consulta.
     *
     * @return el nombre por id; las cuentas que ya no existen no aparecen
     */
    Map<UUID, String> displayNamesOf(Collection<UUID> userIds);
}
