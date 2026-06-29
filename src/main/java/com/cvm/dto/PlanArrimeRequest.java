package com.cvm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanArrimeRequest {
    @NotNull(message = "La cuota mensual es obligatoria")
    @Min(value = 1, message = "La cuota mensual debe ser mayor a 0")
    private Double cuotaMensualAsignada;
}