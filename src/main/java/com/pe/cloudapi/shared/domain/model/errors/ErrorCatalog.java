package com.pe.cloudapi.shared.domain.model.errors;

/**
 * Contrato de un catálogo de errores.
 *
 * <p>Cada bounded context implementa el suyo con un enum, de modo que los
 * errores que puede producir estén enumerados en un solo sitio y sean
 * revisables de un vistazo, en vez de dispersos en mensajes escritos a mano.
 *
 * <p>El {@link #code()} es lo que consume el cliente: es estable y se puede
 * comparar por igualdad. El mensaje es para humanos y puede cambiar de
 * redacción sin romper a nadie.
 */
public interface ErrorCatalog {

    /** Identificador estable, del estilo {@code MONITORING_ROOM_NOT_FOUND}. */
    String code();

    /** Qué clase de error es, para que la capa de interfaces lo traduzca. */
    ErrorKind kind();

    /** Plantilla del mensaje, con marcadores de {@link String#format}. */
    String messageTemplate();

    /**
     * Construye la excepción correspondiente. Permite lanzarla desde el sitio
     * del error sin ceremonia:
     *
     * <pre>{@code throw MonitoringError.ROOM_NOT_FOUND.with(roomId);}</pre>
     *
     * @param args valores que rellenan la plantilla
     * @return la excepción lista para lanzar
     */
    default DomainException with(Object... args) {
        return new DomainException(this, args);
    }
}
