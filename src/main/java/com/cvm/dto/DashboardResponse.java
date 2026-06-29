package com.cvm.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    // Métricas Generales
    private int totalBrigadasRegistradas;
    private int totalBrigadasMorosas;

    // Métricas de Oro
    private Double totalOroRecaudadoInscripciones;
    private Double totalOroRecaudadoArrime;
    private Double totalOroDeudaArrime;

    // Lista de atención para la UI
    private List<BrigadaMorosaDTO> topMorosos;
}