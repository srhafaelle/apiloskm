package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VentaRequest {
    @NotBlank(message = "El tipo de venta es obligatorio")
    private String tipoVenta; // "BRIGADA", "MINERO_APOYO", "SUBSIDIO"

    @NotBlank(message = "El ID del producto es obligatorio")
    private String productoId;

    @NotBlank(message = "El centro de distribución es obligatorio")
    private String puntoDistribucionId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    private String beneficiarioId;
    private String beneficiarioNombre;
    private String observaciones;
}