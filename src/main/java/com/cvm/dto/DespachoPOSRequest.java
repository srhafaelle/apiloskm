package com.cvm.dto;

import com.cvm.model.TipoDespacho;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DespachoPOSRequest {
    @NotNull(message = "El tipo de despacho es obligatorio")
    private TipoDespacho tipoDespacho;
    //comentario dde cambio
    private String productoId;
    private Double cantidad;
    private String puntoDistribucionId;

    private String beneficiarioId; // Puede ser nulo si es subsidio puro
    private String beneficiarioNombre; // Nombre o texto libre descriptivo
    private String observaciones;
}