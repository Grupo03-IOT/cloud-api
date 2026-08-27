package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Convierte las excepciones en respuestas con un formato uniforme.
 *
 * <p>Todo error sale con un código de catálogo, venga del dominio o del
 * transporte. Aquí no se inventa ninguno: este manejador solo elige qué entrada
 * corresponde y la traduce a HTTP.
 *
 * <p>Importa especialmente para la ingesta: el Edge reintenta el lote entero
 * ante cualquier respuesta que no sea 2xx, así que un error mal clasificado
 * puede dejarlo reintentando en bucle. Los errores de quien llama salen como
 * 4xx —reintentarlos no arreglará nada— y solo los fallos nuestros como 500,
 * donde reintentar sí tiene sentido.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Error de negocio con entrada de catálogo. Es el camino normal: el código
     * y la clasificación vienen del propio error, no se deciden aquí.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex,
                                                      HttpServletRequest request) {
        return respond(ex.getError(), ex.getMessage(), request, List.of());
    }

    /** Falla la validación de los recursos: la petición está mal formada. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return respond(ApiError.VALIDATION_FAILED,
                ApiError.VALIDATION_FAILED.messageTemplate(), request, details);
    }

    /** JSON ilegible o tipos incompatibles en el cuerpo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return respond(ApiError.MALFORMED_REQUEST,
                ApiError.MALFORMED_REQUEST.messageTemplate(), request, List.of());
    }

    /**
     * Falta un parámetro obligatorio de la consulta, de la ruta o una cabecera.
     */
    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleMissingValue(MissingRequestValueException ex,
                                                            HttpServletRequest request) {
        String name = ex instanceof MissingServletRequestParameterException missing
                ? missing.getParameterName() : "unknown";
        DomainException error = ApiError.MISSING_PARAMETER.with(name);
        return respond(error.getError(), error.getMessage(), request, List.of());
    }

    /**
     * Un parámetro de la ruta o de la consulta no se pudo convertir al tipo
     * esperado: un identificador que no es UUID, una fecha mal formada.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String expected = ex.getRequiredType() == null
                ? "the expected type" : ex.getRequiredType().getSimpleName();
        DomainException error = ApiError.INVALID_PARAMETER.with(ex.getName(), expected);
        return respond(error.getError(), error.getMessage(), request, List.of());
    }

    /**
     * Cualquier otro fallo. Se registra completo porque, a diferencia de los
     * anteriores, aquí el problema es nuestro y el Edge va a reintentar.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex,
                                                          HttpServletRequest request) {
        log.error("Fallo no controlado en {}", request.getRequestURI(), ex);
        return respond(ApiError.INTERNAL_ERROR,
                ApiError.INTERNAL_ERROR.messageTemplate(), request, List.of());
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCatalog error, String message,
                                                  HttpServletRequest request,
                                                  List<String> details) {
        return ErrorResponse.build(
                ErrorKindHttpMapper.toStatus(error.kind()),
                error.code(),
                message,
                request,
                details);
    }
}
