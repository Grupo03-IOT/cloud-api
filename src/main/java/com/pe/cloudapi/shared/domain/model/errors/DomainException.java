package com.pe.cloudapi.shared.domain.model.errors;

import java.io.Serial;

/**
 * Error de negocio, siempre respaldado por una entrada de catálogo.
 *
 * <p>No se lanza directamente: se construye desde el catálogo con
 * {@link ErrorCatalog#with(Object...)}, lo que garantiza que todo error tenga
 * código y clasificación.
 */
public class DomainException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ErrorCatalog error;

    DomainException(ErrorCatalog error, Object... args) {
        super(format(error, args));
        this.error = error;
    }

    public ErrorCatalog getError() {
        return error;
    }

    public String getCode() {
        return error.code();
    }

    public ErrorKind getKind() {
        return error.kind();
    }

    /**
     * Rellena la plantilla. Si los argumentos no encajan se devuelve la
     * plantilla en crudo: un error al formatear un error no puede tapar el
     * error original.
     */
    private static String format(ErrorCatalog error, Object... args) {
        if (args == null || args.length == 0) {
            return error.messageTemplate();
        }
        try {
            return String.format(error.messageTemplate(), args);
        } catch (RuntimeException ex) {
            return error.messageTemplate();
        }
    }
}
