package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
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
    private String unidad;
    private Double precioOro;

    @Builder.Default
    private boolean activo = true;

    // Control de Inventario Global
    private Double stockFisico;
    private Double stockComprometido;

    @Builder.Default
    private List<StockCentro> inventarioPorCentro = new ArrayList<>();

    private Double cantidadTotalDespachada;
    private Double oroRecaudadoHistorico;

   // @Version
    //private Long version;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockCentro {
        private String puntoDistribucionId;
        private String nombreCentro;
        private Double cantidad; // OJO: Si vas a usar múltiples tanques en el futuro, esto también debería dividirse en Fisico y Comprometido. Por ahora lo dejamos así.
    }

    // @Transient evita que MongoDB intente guardar este campo,
    // pero Spring Boot sí lo enviará en el JSON al Frontend.
    @Transient
    public Double getStockDisponible() {
        double fisico = (this.stockFisico != null) ? this.stockFisico : 0.0;
        double comprometido = (this.stockComprometido != null) ? this.stockComprometido : 0.0;
        return fisico - comprometido;
    }
    //comentario dde cambio
    // Este método lo ajustamos para que sume el inventario FÍSICO de todos los tanques/centros
    public void recalcularStockGlobal() {
        if (this.inventarioPorCentro != null) {
            this.stockFisico = this.inventarioPorCentro.stream()
                    .mapToDouble(StockCentro::getCantidad)
                    .sum();
        } else {
            this.stockFisico = 0.0;
        }
    }
}