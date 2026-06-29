package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    // Métricas Estadísticas (Se actualizan solas con cada despacho)
    private Double cantidadTotalDespachada = 0.0;
    private Double oroRecaudadoHistorico = 0.0;
}