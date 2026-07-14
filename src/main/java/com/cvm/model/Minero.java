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

    // Si un minero puede estar sin brigada temporalmente, este campo puede ser null
    private String brigadaActualId;

    @Builder.Default
    private boolean esFundador = false;

    // ==========================================
    // NUEVOS CAMPOS: IDENTIFICACIÓN Y ESTADO
    // ==========================================
    @Indexed(unique = true)
    private String numeroUnicoRegistro; // Ejemplo: MIN-A8F93K (Para el carnet QR)

    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Builder.Default
    private boolean operacionesParalizadas = false; // Controla si se le suma arrime o no

    // ==========================================
    // CONTROL DE INSCRIPCIÓN Y PAGOS EN ORO
    // ==========================================
    @Builder.Default
    private Double cuotaInscripcionOro = 20.0; // Fijo: 20 gramos

    @Builder.Default
    private Double oroPagadoHastaLaFecha = 0.0;

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

    /**
     * Regla de Negocio: Bloqueo automático para venta de insumos en el POS.
     * Se bloquea si no ha pagado la inscripción O si debe 2 o más cuotas de arrime.
     */
    @JsonIgnore
    public boolean puedeComprarInsumos() {
        if (!inscripcionSolvente()) {
            return false;
        }
        // Margen de tolerancia: si tiene 2 o más meses en deuda, se bloquea.
        return obtenerMesesEnDeuda().size() < 2;
    }
}