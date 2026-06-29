package com.cvm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrigadaMorosaDTO {
    private String brigadaId;
    private String nombreBrigada;
    private String numeroUnicoRegistro;
    private Double deudaTotalOro;
    private int mesesAtraso;
}