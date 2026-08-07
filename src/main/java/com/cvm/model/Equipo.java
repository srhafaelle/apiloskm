package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {
    private TipoEquipo tipo;
    private Integer cantidad;
    @Builder.Default
    private boolean activo = true;

    // El equipo está físicamente en un sector específico
    private String sectorMineroId;
}