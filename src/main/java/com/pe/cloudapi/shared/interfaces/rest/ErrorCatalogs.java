package com.pe.cloudapi.shared.interfaces.rest;

import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalog;
import com.pe.cloudapi.shared.domain.model.errors.ErrorCatalogSource;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Todos los catálogos de errores, indexados por código.
 */
@Component
public class ErrorCatalogs {

    private final Map<String, ErrorCatalog> byCode;

    /**
     * @throws IllegalStateException si dos catálogos declaran el mismo código,
     *         que solo puede pasar si dos contextos comparten prefijo
     */
    public ErrorCatalogs(List<ErrorCatalogSource> sources) {
        this.byCode = Stream.concat(Stream.of((ErrorCatalogSource) ApiError::values), sources.stream())
                .flatMap(source -> Arrays.stream(source.entries()))
                .collect(Collectors.toUnmodifiableMap(ErrorCatalog::code, Function.identity()));
    }

    public Optional<ErrorCatalog> find(String code) {
        return Optional.ofNullable(byCode.get(code));
    }
}
