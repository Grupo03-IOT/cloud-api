package com.pe.cloudapi.shared.domain.model.errors;

/**
 * Un catálogo de errores que se publica para que puedan resolverse sus códigos.
 *
 * <p>Cada bounded context <strong>se apunta</strong>; nadie lo descubre por él.
 * Es lo que permite que {@code shared} traduzca un código sin conocer a ningún
 * contexto: la dependencia va en la única dirección admitida.
 */
@FunctionalInterface
public interface ErrorCatalogSource {

    ErrorCatalog[] entries();
}
