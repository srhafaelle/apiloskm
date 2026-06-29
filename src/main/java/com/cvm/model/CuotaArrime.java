package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaArrime {

    @Builder.Default
    private String idCuota = UUID.randomUUID().toString();

    private String periodo; // Ej: "Junio 2026"
    private Double montoExigidoOro; // Lo que se le determinó ese mes

    @Builder.Default
    private Double montoPagadoOro = 0.0;

    private LocalDate fechaVencimiento;

    private LocalDateTime fechaPago;
    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE;

    private LocalDateTime fechaPagoCompletado;

    // Método útil para saber cuánto falta para cerrar este mes
    public Double getSaldoPendiente() {
        return Math.max(0, montoExigidoOro - montoPagadoOro);
    }
}