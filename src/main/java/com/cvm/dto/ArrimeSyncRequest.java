package com.cvm.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArrimeSyncRequest {
    //comentario dde cambio
    private String mineroId;
    private String numeroTicket;
    private Double montoOro;
    private String tipoCobro;
    private LocalDateTime fechaCobroLocal;
    private String mineroNombre;
    private String mineroCedula;
}