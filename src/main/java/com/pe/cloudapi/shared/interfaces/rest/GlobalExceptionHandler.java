package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.DomainException;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.interfaces.rest.ErrorResponse.ErrorDetail;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Convierte las excepciones en respuestas con un formato uniforme.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Pattern CAMEL_HUMP = Pattern.compile("([a-z0-9])([A-Z])");

    private final ErrorCatalogs catalogs;

    /**
     * Error de negocio con entrada de catálogo. Es el camino normal: el código
     * y la clasificación vienen del propio error, no se deciden aquí.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex,
                                                      HttpServletRequest request) {
        return respond(ex.getError(), ex.getMessage(), request);
    }

    /**
     * Falla la validación del cuerpo: sale un detalle por campo incumplidor.
     *
     * <p>Varios a la vez, a propósito. Devolver solo el primero obliga a quien
     * integra a arreglar, reenviar y descubrir el siguiente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toDetail)
                .toList();
        return respondWith(ApiError.VALIDATION_FAILED,
                ApiError.VALIDATION_FAILED.messageTemplate(), request, errors);
    }

    /**
     * Falla la validación de un parámetro: {@code @Positive} en un
     * {@code @RequestParam}, por ejemplo.
     *
     * <p>Sin este manejador acabaría en el genérico y saldría un 500 por un
     * error de quien llama, con el Edge reintentando en bucle.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        List<ErrorDetail> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> ErrorDetail.of(
                                result.getMethodParameter().getParameterName(),
                                resolve(error.getDefaultMessage(),
                                        constraintOf(error.getCodes())))))
                .toList();
        return respondWith(ApiError.VALIDATION_FAILED,
                ApiError.VALIDATION_FAILED.messageTemplate(), request, errors);
    }

    /**
     * JSON ilegible o un valor que no encaja con su tipo.
     *
     * <p>Jackson sabe en qué campo se atascó, así que se señala en vez de
     * responder solo «no se pudo leer el cuerpo» —que es lo mismo que se
     * respondería ante un JSON truncado, y no son el mismo problema—. Su
     * mensaje, en cambio, no se propaga: nombra clases de Java y eso no es
     * asunto de quien llama.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                          HttpServletRequest request) {
        return locate(ex)
                .map(error -> respondWith(ApiError.MALFORMED_REQUEST,
                        ApiError.MALFORMED_REQUEST.messageTemplate(), request, List.of(error)))
                .orElseGet(() -> respond(ApiError.MALFORMED_REQUEST,
                        ApiError.MALFORMED_REQUEST.messageTemplate(), request));
    }

    /**
     * Falta un parámetro obligatorio de la consulta, de la ruta o una cabecera.
     */
    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleMissingValue(MissingRequestValueException ex,
                                                            HttpServletRequest request) {
        String name = switch (ex) {
            case MissingServletRequestParameterException missing -> missing.getParameterName();
            case MissingPathVariableException missing -> missing.getVariableName();
            default -> "unknown";
        };
        DomainException error = ApiError.MISSING_PARAMETER.with(name);
        return respondWith(error.getError(), error.getMessage(), request,
                List.of(new ErrorDetail(name, error.getError().code(), error.getMessage())));
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
        return respondWith(error.getError(), error.getMessage(), request,
                List.of(new ErrorDetail(ex.getName(), error.getError().code(), error.getMessage())));
    }

    /**
     * Falta la credencial o no es válida.
     *
     * <p>Llega por dos caminos: se lo pasa {@link UnauthorizedEntryPoint} cuando
     * la seguridad rechaza antes del controlador, y llega solo cuando la lanza
     * un {@code @PreAuthorize}. Sin este manejador, el segundo caso caería en el
     * cajón de sastre y <strong>un 401 saldría como 500</strong>.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticated(HttpServletRequest request) {
        return transport(TransportError.UNAUTHORIZED, request);
    }

    /** La credencial es válida y no alcanza. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest request) {
        return transport(TransportError.FORBIDDEN, request);
    }

    /**
     * La ruta no corresponde a ningún endpoint.
     *
     * <p>Los cuatro manejadores siguientes atrapan excepciones del propio
     * framework. Sin ellos caerían en el cajón de sastre de abajo y saldrían
     * como <strong>500</strong>, que para el Edge significa «vuelve a
     * intentarlo»: se quedaría reintentando en bucle un fallo suyo que no va a
     * arreglarse nunca.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex,
                                                          HttpServletRequest request) {
        return transport(TransportError.ENDPOINT_NOT_FOUND, request,
                ex.getHttpMethod(), ex.getResourcePath());
    }

    /** La ruta existe, pero no admite ese verbo. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return transport(TransportError.METHOD_NOT_ALLOWED, request, ex.getMethod());
    }

    /**
     * El cuerpo viene en un tipo de contenido que no sabemos leer.
     *
     * <p>Puede llegar sin ninguno, y entonces el mensaje diría «'null' is not
     * supported», que confunde: el problema es que falta, no que valga nulo.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        Object type = ex.getContentType() == null ? "none" : ex.getContentType();
        return transport(TransportError.UNSUPPORTED_MEDIA_TYPE, request, type);
    }

    /** El cliente pide en su {@code Accept} algo que no sabemos producir. */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        return transport(TransportError.NOT_ACCEPTABLE, request);
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
                ApiError.INTERNAL_ERROR.messageTemplate(), request);
    }

    /** El incumplimiento de un campo del cuerpo. */
    private ErrorDetail toDetail(FieldError error) {
        return ErrorDetail.of(
                asWireName(error.getField()),
                resolve(error.getDefaultMessage(), error.getCode()));
    }

    /**
     * La entrada de catálogo que corresponde a un incumplimiento.
     *
     * <p>Dos caminos, un único resultado. Si la anotación llevaba un código
     * —{@code @NotNull(message = "MONITORING_READING_PERIOD_REQUIRED")}— el
     * registro lo encuentra. Si no llevaba nada, se busca la entrada de la
     * restricción que falló: {@code @NotBlank} da {@link ApiError#NOT_BLANK}.
     *
     * <p>La pregunta es <em>«¿está declarado?»</em> y no <em>«¿parece un
     * código?»</em>. Antes se decidía por la forma del texto —solo
     * mayúsculas— y eso confundía un mensaje escrito en mayúsculas con un
     * código inexistente. Ahora manda el catálogo: <strong>lo que no está
     * declarado no puede salir</strong>.
     */
    private ErrorCatalog resolve(String message, String constraint) {
        return catalogs.find(message == null ? "" : message)
                .orElseGet(() -> ApiError.forConstraint(constraint));
    }

    /**
     * Busca en la cadena de causas el campo donde Jackson se atascó.
     *
     * @return el sitio, o vacío si el JSON no llegó a tener estructura
     */
    private static java.util.Optional<ErrorDetail> locate(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
                return java.util.Optional.of(
                        ErrorDetail.of(asWireName(pathOf(jackson)), ApiError.MALFORMED_FIELD));
            }
        }
        return java.util.Optional.empty();
    }

    /** La ruta de Jackson en la misma notación que usa la validación. */
    private static String pathOf(JacksonException ex) {
        return ex.getPath().stream()
                .map(step -> step.getPropertyName() == null
                        ? "[" + step.getIndex() + "]"
                        : "." + step.getPropertyName())
                .collect(Collectors.joining())
                .replaceFirst("^\\.", "");
    }

    /** {@code readings[0].roomId} → {@code readings[0].room_id}. */
    private static String asWireName(String field) {
        return CAMEL_HUMP.matcher(field).replaceAll("$1_$2").toLowerCase(Locale.ROOT);
    }

    /**
     * El nombre de la restricción, de la lista que Spring ordena de más
     * específica a más general: {@code Positive.periodS}, {@code Positive.int},
     * {@code Positive}. La última es la que sirve para buscar en el catálogo.
     */
    private static String constraintOf(String[] codes) {
        return codes == null || codes.length == 0 ? "" : codes[codes.length - 1];
    }

    /**
     * Respuesta a un error de transporte: trae su propio estado, así que no pasa
     * por {@link ErrorKindHttpMapper}.
     */
    private ResponseEntity<ErrorResponse> transport(TransportError error,
                                                    HttpServletRequest request,
                                                    Object... args) {
        return ErrorResponse.of(error.status(), error.code(), error.message(args), request);
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCatalog error, String message,
                                                  HttpServletRequest request) {
        return ErrorResponse.of(
                ErrorKindHttpMapper.toStatus(error.kind()), error.code(), message, request);
    }

    private ResponseEntity<ErrorResponse> respondWith(ErrorCatalog error, String message,
                                                      HttpServletRequest request,
                                                      List<ErrorDetail> errors) {
        return ErrorResponse.withDetails(
                ErrorKindHttpMapper.toStatus(error.kind()), error.code(), message, request, errors);
    }
}
