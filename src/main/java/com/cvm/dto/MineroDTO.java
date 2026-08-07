package com.cvm.dto;

import com.cvm.model.Equipo;
import com.cvm.model.PlanArrime;
import com.cvm.model.TipoMinero;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MineroDTO {
    private String id;
    private String nombres;
    private String apellidos;
    private String cedula;
    private String cargo;
    private TipoMinero tipoMinero;
    private String numeroUnicoRegistro;
    private boolean operacionesParalizadas;
    private Double cuotaInscripcionOro;

    private List<String> sectorMineroIds;
    private List<Equipo> equipos;
    private PlanArrime planArrime;

    // ==========================================
    // CAMPOS CALCULADOS DINÁMICAMENTE POR EL BACKEND
    // ==========================================
    private Double oroPagadoHastaLaFecha;
    private Double deudaInscripcionRestante;
    private boolean inscripcionSolvente;
    private Double litrosCompradosCicloActual;
    private Double deudaArrimeTotal;
}