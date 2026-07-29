package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DespachoFisicoRequest {
    //comentario dde cambio
    @NotNull(message = "La cantidad a despachar es obligatoria")
    @Min(value = 1, message = "Debe despachar al menos 1 unidad (litro/gramo)")
    private Double cantidadDespachada;

}