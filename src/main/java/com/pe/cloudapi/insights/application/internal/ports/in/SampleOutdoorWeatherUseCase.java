package com.pe.cloudapi.insights.application.internal.ports.in;

/**
 * Puerto de entrada: tomar una muestra del tiempo exterior y guardarla.
 *
 * <p>Existe porque el proveedor solo sirve el momento actual. Sin ir muestreando
 * no habría histórico contra el que correlacionar.
 */
public interface SampleOutdoorWeatherUseCase {

    /**
     * @return {@code true} si se guardó una observación nueva
     */
    boolean execute();
}
