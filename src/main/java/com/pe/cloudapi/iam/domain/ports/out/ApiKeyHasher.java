package com.pe.cloudapi.iam.domain.ports.out;

/**
 * Generar claves de máquina y reducirlas a su hash.
 *
 * <p>El hash debe ser <strong>determinista</strong>: la credencial se busca por
 * él en cada petición, así que no sirve un algoritmo con sal.
 */
public interface ApiKeyHasher {

    /** @return una clave nueva en claro; no queda guardada en ninguna parte */
    String generate();

    String hash(String apiKey);
}
