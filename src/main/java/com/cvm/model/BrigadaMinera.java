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
    //comentario dde cambio
    @Id
    private String id;

    private String nombreBrigada; // Por ahora será el número asignado al minero responsable

    @Indexed(unique = true)
    private String numeroUnicoRegistro; // Ejemplo: BRG-A8F93K (Para el QR)

    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    // ===== NUEVA ESTRUCTURA DE MIEMBROS =====
    @Builder.Default
    private Set<String> fundadoresIds = new HashSet<>();  // máximo 12, inamovibles

    @Builder.Default
    private Set<String> empleadosIds = new HashSet<>();
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


    // ==========================================
    // NUEVO: PLAN DE ARRIME MENSUAL
    // ==========================================
    private PlanArrime planArrime;



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

    @JsonIgnore
    public Set<String> getTodosLosMinerosIds() {
        Set<String> todos = new HashSet<>(fundadoresIds);
        todos.addAll(empleadosIds);
        return todos;
    }
}