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

    private String tipoDeCaja; // NUEVO: "Combustible - Gasoil", "Combustible - Gasolina", "Pequeña Minería", "Utilidades", "Gastos"

    private String motivo;
    private String referencia;
    private String origenDestino;

    private String usuarioResponsable;

    @Builder.Default
    private LocalDateTime fechaTransaccion = LocalDateTime.now();
}