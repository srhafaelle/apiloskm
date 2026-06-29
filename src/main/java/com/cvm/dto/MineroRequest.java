package com.cvm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MineroRequest {
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;
}