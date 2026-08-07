package com.cvm.dto;
import com.cvm.model.TipoDeArrime;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArrimeSyncRequest {
    private String mineroId;
    private String mineroNombre;
    private String mineroCedula;

    // El código único temporal que genera Flutter (ej. V123-AE-040826)
    private String numeroSeguimiento;

    private String sectorMineroId;
    private TipoDeArrime tipoDeArrime;

    // Puede venir nulo si es INSCRIPCION, pero es OBLIGATORIO si es DRAGA
    private Double produccion;

    // Lo que Flutter calculó (aunque el backend siempre tiene la última palabra)
    private Double montoOro;

    private LocalDateTime fechaCobroLocal;
}