package com.cvm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TesoreriaResponse {
    private Double totalVentas;
    private Double totalInscripciones;
    private Double totalArrime;
}