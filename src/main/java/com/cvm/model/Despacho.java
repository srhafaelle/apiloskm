package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Despacho {
    private String productoId;
    private String nombreProducto;
    private Double cantidadEntregada;
    private Double costoEnOro; // Lo que costó en ese momento

    @Builder.Default
    private LocalDateTime fechaDespacho = LocalDateTime.now();

    private String despachadoPorUsuarioId; // Quién del sistema autorizó la salida
}