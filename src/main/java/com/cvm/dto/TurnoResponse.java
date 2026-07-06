package com.cvm.dto;
import com.cvm.model.EstadoTurno;
import com.cvm.model.Turno;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TurnoResponse {
    private String id;
    private String usuarioCajeroId;
    private String nombreCentro;
    private EstadoTurno estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private Double totalOroRecaudado;
    private Integer cantidadOperaciones;
    private String puntoDistribucionId;
    private List<Turno.ResumenInsumo> resumenInsumos;
}