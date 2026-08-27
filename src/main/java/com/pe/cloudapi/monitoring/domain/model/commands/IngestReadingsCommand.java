package com.pe.cloudapi.monitoring.domain.model.commands;

import com.pe.cloudapi.monitoring.domain.model.entities.Device;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.AcousticMetrics;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Climate;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.DataQuality;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.Occupancy;
import com.pe.cloudapi.monitoring.domain.model.valueobjects.ThermalComfort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Guardar un lote de lecturas subido por el Edge.
 *
 * <p>Es <strong>un</strong> comando, no uno por lectura: la intención es
 * ingerir el lote entero, y se resuelve en una única transacción.
 *
 * <p>A diferencia de los demás comandos de este paquete, a este <strong>no lo
 * recibe un agregado sino un caso de uso</strong>: trae la sala por su código y
 * resolverlo exige consultar el repositorio, y hasta dar de alta una sala
 * nueva. Una vez resuelta, cada lectura se convierte en un
 * {@link RecordRoomReadingCommand} con
 * {@link Reading#toRecordCommand(java.util.UUID)}.
 *
 * @param readings los minutos que llegaron en el lote
 */
public record IngestReadingsCommand(List<Reading> readings) {

    /**
     * Un minuto de una sala.
     *
     * <p>La sala viene por su <strong>código</strong>, no por su identificador:
     * resolverlo puede implicar dar de alta una sala nueva, y esa es una
     * decisión que solo el caso de uso puede tomar.
     *
     * @param roomCode código de la sala, tal como lo lleva el firmware
     * @param ts       minuto que describe la lectura, en UTC
     * @param periodS  duración del periodo en segundos
     * @param device   estado del módulo; nulo si la lectura no lo declara
     */
    public record Reading(
            String roomCode,
            OffsetDateTime ts,
            int periodS,
            Device device,
            AcousticMetrics acoustic,
            Climate climate,
            ThermalComfort comfort,
            Occupancy occupancy,
            DataQuality quality
    ) {

        /**
         * Produce el comando que sí recibe el agregado, ya con la sala
         * resuelta.
         *
         * <p>Es la misma lectura antes y después de traducir el código a
         * identificador; tenerlo aquí evita que quien la traduzca vuelva a
         * enumerar los campos a mano y se deje alguno.
         *
         * @param roomId identificador de la sala, ya resuelto
         * @return el comando listo para construir el agregado
         */
        public RecordRoomReadingCommand toRecordCommand(UUID roomId) {
            return new RecordRoomReadingCommand(
                    roomId, ts, periodS, acoustic, climate, comfort, occupancy, quality);
        }

        /**
         * Estado del módulo que reporta por la sala.
         *
         * <p>Los contadores llegan ya calculados por el Edge, que es la única
         * capa que ve la secuencia completa de lotes.
         *
         * @param code        identificador del firmware
         * @param fwVersion   versión declarada
         * @param lastSeen    último lote que le llegó al Edge
         * @param lastSeq     secuencia de ese lote
         * @param lostBatches lotes perdidos acumulados
         */
        public record Device(
                String code,
                String fwVersion,
                OffsetDateTime lastSeen,
                Long lastSeq,
                Long lostBatches
        ) {
        }
    }
}
