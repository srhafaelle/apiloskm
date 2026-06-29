package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanArrime {

    @Builder.Default
    private boolean activo = false;

    private Double cuotaMensualAsignada; // Gramos de oro mensuales según su capacidad

    private LocalDate fechaInicioPlan;
}