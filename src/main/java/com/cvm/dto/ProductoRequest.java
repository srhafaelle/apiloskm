package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoRequest {
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "Debe especificar la unidad (LITROS, GRAMOS, etc.)")
    private String unidad;

    @NotNull(message = "El precio en oro es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precioOro;
    // ... tus campos anteriores (nombre, descripcion, precioOro, unidad, activo)

    // Control de Inventario
    private Double stockDisponible = 0.0;

    // Métricas Estadísticas (Se actualizan solas con cada despacho)
    private Double cantidadTotalDespachada = 0.0;
    private Double oroRecaudadoHistorico = 0.0;
}