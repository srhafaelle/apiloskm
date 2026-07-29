package com.cvm.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArrimeTicketRequest {
    @NotNull(message = "El monto en oro es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    private Double montoOro;

    @NotBlank(message = "El número de ticket es obligatorio")
    private String numeroTicket;

    private String tipoCobro; // ESPONTANEO, PORCENTAJE_DIARIO
    //comentario dde cambio
    @NotNull(message = "La fecha local de cobro es obligatoria")
    private LocalDateTime fechaCobroLocal;
}