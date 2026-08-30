package com.pe.cloudapi.monitoring.application.internal.ports.in;

import com.pe.cloudapi.monitoring.application.internal.results.IngestResult;
import com.pe.cloudapi.monitoring.domain.model.commands.IngestReadingsCommand;

/**
 * Puerto de entrada: guardar el lote de lecturas que sube el Edge.
 *
 * <p>El controlador depende de esta interfaz y no de la implementación, así que
 * la capa de transporte no sabe cómo se resuelve la operación. Y hace posible
 * probarla con un doble sin levantar nada.
 */
public interface IngestReadingsUseCase {

    IngestResult execute(IngestReadingsCommand command);
}
