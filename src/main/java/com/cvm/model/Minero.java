package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mineros")
public class Minero {

    @Id
    private String id;

    private String nombres;
    private String apellidos;

    @Indexed(unique = true)
    private String cedula;
    private String cargo;
    private String brigadaActualId;

    @Builder.Default
    private TipoMinero tipoMinero = TipoMinero.TRABAJADOR;

    @Indexed(unique = true)
    private String numeroUnicoRegistro;

    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Builder.Default
    private boolean operacionesParalizadas = false;

    // --- DEUDA DE INSCRIPCIÓN ---
    @Builder.Default
    private Double cuotaInscripcionOro = 20.0; // Valor fijo de inscripción

    // --- UBICACIONES Y EQUIPOS ---
    // Sectores generales a los que pertenece (si aplica)
    @Builder.Default
    private List<String> sectorMineroIds = new ArrayList<>();

    // Aquí guardamos sus dragas, bombas, etc.
    @Builder.Default
    private List<Equipo> equipos = new ArrayList<>();

    private PlanArrime planArrime;

    public void generarNumeroUnico() {
        if (this.numeroUnicoRegistro == null) {
            String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.numeroUnicoRegistro = "MIN-" + uuidPart.substring(0,4) + "-" + uuidPart.substring(4);
        }
    }
}