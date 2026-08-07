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
@Document(collection = "arrimes_tickets") // Nueva colección para auditoría de tickets
public class Arrime {

    @Id
    private String id;
    //comentario dde cambio
    // Referencia al minero que aportó
    private String mineroId;
    private String mineroNombre;
    private String mineroCedula;


    // Generado en Flutter (Ej: Cedula + Inicial + Fecha) para rastrear offline
    private String numeroSeguimiento;

    // Datos del Ticket de Caja/Campo
    private String numeroTicket;

    //numero de seguimiento




    //nueva logica de sector
    private String sectorMineroId;

    // Puede ser: ESPONTANEO, PORCENTAJE_DIARIO, ABONO_CUOTA
    private TipoDeArrime tipoDeArrime;

    // Quién recolectó el oro (del token JWT al sincronizar)
    private String contralor;

    // La hora real en que se imprimió el ticket offline
    private LocalDateTime fechaCobroLocal;

    private Double produccion;
    private Double montoOro;

    // Cuándo llegó este dato al servidor MongoDB
    @Builder.Default
    private LocalDateTime fechaSincronizacion = LocalDateTime.now();
}