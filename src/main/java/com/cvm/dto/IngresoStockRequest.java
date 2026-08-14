package com.cvm.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IngresoStockRequest {
    private String centroId;
    private String nombreCentro;
    private String numeroDeFactura;
    private String numeroDeControl;
    private String chofer;
    private String idChofer;
    private LocalDateTime fechaDeRecepcion;
    private LocalDateTime fechaDeFacturacion;
    private Double litrajeDeFactura;
    private Double litrajeRecibido; // Este es el que suma al stock real
    private String observacion;
}