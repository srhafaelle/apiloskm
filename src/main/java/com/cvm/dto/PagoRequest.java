package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PagoRequest {
    @NotNull(message = "El monto en oro es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    private Double montoOro;
}