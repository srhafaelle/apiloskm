package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "productos")
public class Producto {

    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private String unidad; // Ej: "LITROS", "GRAMOS", "UNIDAD"

    private Double precioOro; // Precio tasado en gramos de oro

    @Builder.Default
    private boolean activo = true;

    // ... tus campos anteriores (nombre, descripcion, precioOro, unidad, activo)

    // Control de Inventario
    private Double stockDisponible = 0.0;

    @Builder.Default
    private List<StockCentro> inventarioPorCentro = new ArrayList<>();

    // Métricas Estadísticas (Se actualizan solas con cada despacho)
    private Double cantidadTotalDespachada = 0.0;
    private Double oroRecaudadoHistorico = 0.0;

    // Sub-documento para guardar el inventario detallado en MongoDB
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockCentro {
        private String puntoDistribucionId;
        private String nombreCentro;
        private Double cantidad;
    }

    // Método de utilidad para recalcular el stock global automáticamente
    public void recalcularStockGlobal() {
        if (this.inventarioPorCentro != null) {
            this.stockDisponible = this.inventarioPorCentro.stream()
                    .mapToDouble(StockCentro::getCantidad)
                    .sum();
        } else {
            this.stockDisponible = 0.0;
        }
    }
}