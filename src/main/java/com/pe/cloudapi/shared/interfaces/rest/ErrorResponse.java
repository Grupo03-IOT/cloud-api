package com.pe.cloudapi.shared.interfaces.rest;

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
 * @param details   errores campo a campo; nulo si no aplica
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
        List<String> details
) {

    /**
     * Arma la respuesta de error lista para devolver.
     *
     * @param status  código HTTP
     * @param code    identificador del catálogo
     * @param message descripción legible
     * @param request petición que lo provocó, de la que se toma la ruta
     * @param details errores campo a campo; vacío se traduce a nulo para que
     *                el campo no aparezca en el JSON
     * @return la respuesta con su código
     */
    public static ResponseEntity<ErrorResponse> build(HttpStatus status, String code,
                                                      String message,
                                                      HttpServletRequest request,
                                                      List<String> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                details == null || details.isEmpty() ? null : details));
    }

    /** Igual que {@link #build}, para errores sin detalle campo a campo. */
    public static ResponseEntity<ErrorResponse> build(HttpStatus status, String code,
                                                      String message,
                                                      HttpServletRequest request) {
        return build(status, code, message, request, List.of());
    }
}
