package com.cvm.dto;

import com.cvm.model.TipoEquipo;
import lombok.Data;

@Data
public class EquipoRequest {
    private TipoEquipo tipo;
    private Integer cantidad;
    private boolean activo;
    private String sectorMineroId;
}