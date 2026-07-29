package com.cvm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PuntoDistribucionRequest {
    //comentario dde cambio
    @NotBlank(message = "El nombre del centro es obligatorio")
    private String nombre;
    private String ubicacion;
}