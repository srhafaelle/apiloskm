package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Despacho {
    @Id
    private String id;
    private TipoDespacho tipoDespacho;

    private String productoId;
    private String nombreProducto;
    private Double cantidadEntregada;
    private Double costoEnOro;

    private String puntoDistribucionId;
    private String nombrePuntoDistribucion;

    private String beneficiarioId;
    private String beneficiarioNombre;


    private String observaciones;
    private String despachadoPorUsuarioId;
    private LocalDateTime fechaDespacho;





}