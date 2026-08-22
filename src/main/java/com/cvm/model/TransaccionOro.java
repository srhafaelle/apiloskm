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
@Document(collection = "boveda_transacciones")
public class TransaccionOro {

    @Id
    private String id;

    private String tipo; // "ENTRADA" o "SALIDA"
    private Double gramos;

    private String motivo; // Ej: "Recaudación de Arrime", "Compra de Repuestos"
    private String referencia; // Factura, N° de Ticket, Recibo.
    private String origenDestino; // De dónde viene (Ej: "Sector Los KM") o a quién se le paga (Ej: "Ferretería CA")

    private String usuarioResponsable; // Email o ID de quien registró el movimiento

    @Builder.Default
    private LocalDateTime fechaTransaccion = LocalDateTime.now();
}