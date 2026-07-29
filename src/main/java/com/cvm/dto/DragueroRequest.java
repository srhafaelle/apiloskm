package com.cvm.dto;
import lombok.Data;

@Data
public class DragueroRequest {
    //comentario dde cambio
    private String nombres;
    private String apellidos;
    private String cedula;
    private String telefono;
    private String ubicacionActividad;
    private Integer cantidadEquipos;
    private String observaciones;
}