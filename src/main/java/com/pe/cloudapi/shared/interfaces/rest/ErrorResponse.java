package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Formato uniforme de error para toda la API.
 *
 * <p>El campo importante es {@code code}: viene del catálogo de errores del
 * bounded context y es estable. Un cliente compara contra él en vez de parsear
 * el mensaje, que está redactado para humanos y puede cambiar sin previo aviso.
 *
 * <p>La marca de tiempo se sella en UTC igual que el resto del sistema, para no
 * mezclar husos al correlacionar un error con la telemetría de ese momento.
 *
 * @param timestamp cuándo ocurrió, en UTC
 * @param status    código HTTP
 * @param code      identificador estable del catálogo
 * @param message   descripción legible
 * @param path      ruta que lo produjo
 * @param errors    dónde está el problema; ausente cuando afecta a la petición entera
 */
@Schema(description = "Error response")
public record ErrorResponse(

        OffsetDateTime timestamp,

        int status,

        @Schema(description = "Identificador estable del error, del catálogo del "
                + "bounded context", example = "MONITORING_ROOM_NOT_FOUND")
        String code,

        String message,

        String path,

        @Schema(description = "Dónde está el problema, cuando se sabe señalarlo. "
                + "Ausente si el error afecta a la petición entera")
        List<ErrorDetail> errors
) {

    /**
     * El sitio concreto que causó el error.
     *
     * <p>Sale siempre que se sepa señalarlo: un campo del cuerpo que no cumple,
     * un parámetro que falta, uno que no se pudo convertir. Cuando el error es
     * de la petición entera —una sala que no existe— no hay nada que señalar y
     * la lista no aparece.
     *
     * <p>Los tres campos van <strong>siempre</strong>. Es lo que permite que el
     * cliente escriba un solo manejador —{@code traducir(code) ?? message}— sin
     * casos especiales: un código que todavía no sepa traducir cae al mensaje y
     * se ve igual, sin tocar el cliente.
     *
     * @param field   el nombre tal como lo escribió el cliente: en snake_case si
     *                venía en el cuerpo, y tal cual si es un parámetro
     * @param code    estable, para que el cliente traduzca sin parsear el
     *                mensaje
     * @param message en inglés, para quien depura y como respaldo del cliente
     */
    @Schema(description = "Where the problem is")
    public record ErrorDetail(

            @Schema(example = "readings[0].room_id")
            String field,

            @Schema(example = "API_NOT_BLANK")
            String code,

            @Schema(example = "This field cannot be empty")
            String message
    ) {

        /**
         * El detalle de un campo que incumple una entrada del catálogo.
         *
         * <p>Usa la plantilla sin rellenar, así que no vale para entradas con
         * huecos {@code %s}: esas se construyen con el constructor y el mensaje
         * ya formateado.
         */
        public static ErrorDetail of(String field, ErrorCatalog error) {
            return new ErrorDetail(field, error.code(), error.messageTemplate());
        }
    }

    /**
     * Error que afecta a la petición entera: no hay campo que señalar.
     */
    public static ResponseEntity<ErrorResponse> of(HttpStatus status, String code,
                                                   String message,
                                                   HttpServletRequest request) {
        return build(status, code, message, request, List.of());
    }

    /**
     * Error que sí sabe dónde está: uno o varios sitios concretos.
     */
    public static ResponseEntity<ErrorResponse> withDetails(HttpStatus status, String code,
                                                            String message,
                                                            HttpServletRequest request,
                                                            List<ErrorDetail> errors) {
        return build(status, code, message, request, errors);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code,
                                                       String message,
                                                       HttpServletRequest request,
                                                       List<ErrorDetail> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                errors == null || errors.isEmpty() ? null : errors));
    }
}
