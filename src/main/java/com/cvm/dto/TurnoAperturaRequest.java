package com.cvm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class TurnoAperturaRequest {

    @NotBlank(message = "Debe especificar el ID del centro de distribución")
    private String puntoDistribucionId;

    @NotBlank(message = "El nombre del centro es obligatorio")
    private String nombreCentro;


    @NotNull(message = "Debe enviar la lista de productos con su stock inicial")
    @Valid
    private List<ProductoStockInicial> productos;

    @Data
    public static class ProductoStockInicial {
        @NotBlank
        private String productoId;

        private String nombreProducto;  // opcional si el backend puede obtenerlo de otra fuente

        @PositiveOrZero
        private double stockInicial;
    }
}