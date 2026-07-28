package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ventas")
public class Venta {
    @Id
    private String id;
    private String turnoId;
    private String numeroGuia;

    // Relaciones
    private String productoId;
    private String nombreProducto;
    private String puntoDistribucionId;
    private String nombreCentro;
    private String usuarioCajeroId;

    // Datos Financieros y de Inventario
    private Double cantidadSolicitada;
    private Double cantidadEntregada;

    private String nombreChofer;
    private String direccionDestino;

    // Usaremos SOLO este para el costo total
    private Double montoTotalOro;

    // Beneficiario (Minero o Subsidio)
    private String mineroId;
    private String beneficiarioId;
    private String beneficiarioNombre;
    private String observaciones;

    private LocalDateTime fechaVenta;
    private LocalDateTime fechaUltimoDespacho;

    private TipoVenta tipoVenta;
    private EstadoVenta estado;

    private Boolean pagada;

    public Double getCantidadPendiente() {
        double solicitada = this.cantidadSolicitada != null ? this.cantidadSolicitada : 0.0;
        double entregada = this.cantidadEntregada != null ? this.cantidadEntregada : 0.0;
        return solicitada - entregada;
    }
}