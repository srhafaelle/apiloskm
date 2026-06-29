package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DespachoRequest {
    @NotBlank(message = "Debe especificar el ID del producto")
    private String productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad debe ser mayor a 0")
    private Double cantidad;
}