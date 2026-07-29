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
    //comentario dde cambio
    @Builder.Default
    private String idCuota = UUID.randomUUID().toString();

    private String numeroTicket;

    private  String contralorEmailId;

    private String periodo; // Ej: "Junio 2026" (Equivale a 'mesCorrespondiente' en Flutter)

    private Double montoExigidoOro;

    private  LocalDateTime fechaCobroLocal;

    @Builder.Default
    private Double montoPagadoOro = 0.0;

    private LocalDate fechaVencimiento;

    private String tipoCobro;

    @Builder.Default
    private EstadoCuota estado = EstadoCuota.PENDIENTE; // PENDIENTE, PARCIAL, PAGADA

    private LocalDateTime fechaPagoCompletado; // Solo se llena si se pagó toda

    // Método helper
    public Double getSaldoPendiente() {
        if (montoExigidoOro == null) return 0.0;
        return Math.max(0, montoExigidoOro - montoPagadoOro);
    }
}