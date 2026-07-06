package com.cvm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TurnoAperturaRequest {
    @NotBlank(message = "Debe especificar el ID del centro de distribución")
    private String puntoDistribucionId;

    @NotBlank(message = "El nombre del centro es obligatorio")
    private String nombreCentro;
}