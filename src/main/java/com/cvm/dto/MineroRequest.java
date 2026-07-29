package com.cvm.dto;
import com.cvm.model.TipoMinero;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MineroRequest {
    //comentario dde cambio
    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "La cédula es obligatoria")
    private String cedula;

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;

    // --- NUEVOS CAMPOS ---
    private TipoMinero tipoMinero;
    private Double cuotaInscripcionOro; // El admin decide cuánto cobrarle (ej: 0.2, 0.5)

    private String ubicacionTrabajo;
    private String equipos;
}