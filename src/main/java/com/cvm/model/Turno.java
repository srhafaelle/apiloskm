package com.cvm.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "turnos")
public class Turno {
    //comentario dde cambio

    @Id
    private String id;

    // Datos de Apertura y Ubicación (Inmutables)
    private String usuarioCajeroId; // Correo o ID del usuario que abrió la caja
    private String puntoDistribucionId;
    private String nombreCentro;

    private EstadoTurno estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;

    // Totales del Turno (Se actualizan con cada venta)
    @Builder.Default
    private Double totalOroRecaudado = 0.0;

    @Builder.Default
    private Integer cantidadOperaciones = 0; // Cuántos despachos se hicieron

    // Resumen detallado por insumo para el cierre de caja
    @Builder.Default
    private List<ResumenInsumo> resumenInsumos = new ArrayList<>();


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResumenInsumo {
        private String productoId;
        private String nombreProducto;
        private Double totalLitrosEntregados;
    }


}