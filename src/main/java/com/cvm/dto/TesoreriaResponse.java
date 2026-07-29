package com.cvm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesoreriaResponse {
    //comentario dde cambio
    private Double totalVentas;
    private Double totalInscripciones;
    private Double totalArrime;
    private List<ProductoReporteDTO> inventario;
    private List<OperacionReporteDTO> historialOperaciones;
    @Data
    @AllArgsConstructor
    public static class ProductoReporteDTO {
        private String nombre;
        private Double stockDisponible;
        private Double cantidadVendidaPeriodo; // Litros/Gramos vendidos en estas fechas
    }

    @Data
    @AllArgsConstructor
    public static class OperacionReporteDTO {
        private String fecha;
        private String brigada;
        private String tipoOperacion; // "VENTA", "INSCRIPCION", "ARRIME"
        private String detalle; // ej. "200 Litros de Gasoil"
        private Double montoOro;
    }
}