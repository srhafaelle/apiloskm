package com.cvm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrigadaRequest {
    @NotBlank(message = "El nombre de la brigada es obligatorio")
    private String nombreBrigada;

    // Al registrar, podemos pasar el ID del minero líder o responsable inicial
    @NotBlank(message = "Debe asignar un minero responsable (ID)")
    private String mineroResponsableId;
}