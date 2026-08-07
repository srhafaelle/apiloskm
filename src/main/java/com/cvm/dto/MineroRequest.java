package com.cvm.dto;

import com.cvm.model.Equipo;
import com.cvm.model.TipoMinero;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

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

    private TipoMinero tipoMinero;
    private Double cuotaInscripcionOro; // El admin decide cuánto cobrarle (ej: 20.0)

    // --- NUEVAS ESTRUCTURAS COMPATIBLES CON EL SERVICIO ---
    private List<String> sectorMineroIds; // Lista de IDs de los sectores
    private List<Equipo> equipos;         // Lista de objetos Equipo
}