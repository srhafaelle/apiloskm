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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "brigadas")
public class BrigadaMinera {

    @Id
    private String id;

    private String nombreBrigada; // Por ahora será el número asignado al minero responsable

    @Indexed(unique = true)
    private String numeroUnicoRegistro; // Ejemplo: BRG-A8F93K (Para el QR)

    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Almacenamos los IDs de los mineros. Como rotan, es más fácil actualizar un Set de Strings.
    @Builder.Default
    private Set<String> minerosIds = new HashSet<>();

    // ==========================================
    // CONTROL DE INSCRIPCIÓN Y PAGOS EN ORO
    // ==========================================
    @Builder.Default
    private Double cuotaInscripcionOro = 20.0; // Fijo: 20 gramos

    @Builder.Default
    private Double oroPagadoHastaLaFecha = 0.0; // Para permitir abonos parciales

    // ==========================================
    // HISTORIAL DE OPERACIONES
    // ==========================================
    @Builder.Default
    private List<Despacho> historialDespachos = new ArrayList<>();

    // ==========================================
    // NUEVO: PLAN DE ARRIME MENSUAL
    // ==========================================
    private PlanArrime planArrime;

    @Builder.Default
    private List<CuotaArrime> historialCuotas = new ArrayList<>();

    // ==========================================
    // MÉTODOS DE UTILIDAD PARA EL DASHBOARD
    // ==========================================

    public void generarNumeroUnico() {
        if (this.numeroUnicoRegistro == null) {
            String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.numeroUnicoRegistro = "BRG-" + uuidPart.substring(0,4) + "-" + uuidPart.substring(4);
        }
    }

    // Método de utilidad para saber cuánto debe
    public Double getDeudaRestante() {
        return Math.max(0, cuotaInscripcionOro - oroPagadoHastaLaFecha);
    }

    // Método de utilidad para saber si puede recibir despachos
    public boolean inscripcionSolvente() {
        return oroPagadoHastaLaFecha >= cuotaInscripcionOro;
    }
    // Calcula toda la deuda acumulada sumando las cuotas pendientes y vencidas
    public Double obtenerDeudaTotalArrime() {
        if (historialCuotas == null || historialCuotas.isEmpty()) return 0.0;

        return historialCuotas.stream()
                .filter(c -> c.getEstado() != EstadoCuota.PAGADA)
                .mapToDouble(CuotaArrime::getSaldoPendiente)
                .sum();
    }
    // Retorna solo la lista de los meses que no han pagado para mostrarlos en el Frontend
    public List<CuotaArrime> obtenerMesesEnDeuda() {
        if (historialCuotas == null) return new ArrayList<>();

        return historialCuotas.stream()
                .filter(c -> c.getEstado() != EstadoCuota.PAGADA)
                .collect(Collectors.toList());
    }
}