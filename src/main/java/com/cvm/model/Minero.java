package com.cvm.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.stream.Collectors;

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
    private boolean esFundador = false;

    @Indexed(unique = true)
    private String numeroUnicoRegistro;

    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Builder.Default
    private boolean operacionesParalizadas = false;

    @Builder.Default
    private Double cuotaInscripcionOro = 20.0;

    @Builder.Default
    private Double oroPagadoHastaLaFecha = 0.0;

    // ==========================================
    // NUEVOS CAMPOS: OPERATIVIDAD Y EQUIPOS
    // ==========================================
    private String ubicacionTrabajo; // Ej: "Sector La Llovizna"
    private String equipos;          // Ej: "1 Draga de 8 pulgadas, 2 Molinos"

    @Builder.Default
    private Double litrosCompradosCicloActual = 0.0; // Se reinicia al pagar arrime

    // ==========================================
    // HISTORIAL DE OPERACIONES Y ARRIME
    // ==========================================
    @Builder.Default
    private List<Despacho> historialDespachos = new ArrayList<>();

    private PlanArrime planArrime;

    @Builder.Default
    private List<CuotaArrime> historialCuotas = new ArrayList<>();

    // ==========================================
    // MÉTODOS DE UTILIDAD
    // ==========================================
    public void generarNumeroUnico() {
        if (this.numeroUnicoRegistro == null) {
            String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.numeroUnicoRegistro = "MIN-" + uuidPart.substring(0,4) + "-" + uuidPart.substring(4);
        }
    }

    public Double getDeudaRestante() {
        return Math.max(0, cuotaInscripcionOro - oroPagadoHastaLaFecha);
    }

    public boolean inscripcionSolvente() {
        return oroPagadoHastaLaFecha >= cuotaInscripcionOro;
    }

    public Double obtenerDeudaTotalArrime() {
        if (historialCuotas == null || historialCuotas.isEmpty()) return 0.0;
        return historialCuotas.stream()
                .filter(c -> c.getEstado() != EstadoCuota.PAGADA)
                .mapToDouble(CuotaArrime::getSaldoPendiente)
                .sum();
    }

    public List<CuotaArrime> obtenerMesesEnDeuda() {
        if (historialCuotas == null) return new ArrayList<>();
        return historialCuotas.stream()
                .filter(c -> c.getEstado() != EstadoCuota.PAGADA)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean puedeComprarInsumos() {
        if (!inscripcionSolvente()) return false;
        return obtenerMesesEnDeuda().size() < 2;
    }
}