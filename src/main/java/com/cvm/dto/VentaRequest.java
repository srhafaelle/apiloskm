package com.cvm.dto;

import com.cvm.model.TipoVenta;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VentaRequest {

    // Cambiamos @NotBlank por @NotNull porque es un Enum
    @NotNull(message = "El tipo de venta es obligatorio")
    private TipoVenta tipoVenta;

    @NotBlank(message = "El ID del producto es obligatorio")
    private String productoId;

    @NotBlank(message = "El centro de distribución es obligatorio")
    private String puntoDistribucionId;

    // Dejamos solo cantidadSolicitada para alinear con la Entidad Venta
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Double cantidadSolicitada;

    private String mineroId;
    private String beneficiarioId;
    private String beneficiarioNombre;
    private String observaciones;
    private String nombreChofer;
    private String direccionDestino;

    @NotNull(message = "El monto en oro es obligatorio")
    private Double montoOro;
}