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
@Document(collection = "ventas")
public class Venta {
    @Id
    private String id;
    private String turnoId;

    private String numeroGuia; // Ej: "GUI-10025" para imprimir
    private String tipoVenta;  // "BRIGADA", "APOYO_MINERO", "SUBSIDIO"

    // Relaciones
    private String productoId;
    private String nombreProducto;
    private String puntoDistribucionId;
    private String nombreCentro;
    private String usuarioCajeroId; // Quien hizo la venta

    // Datos Financieros y de Inventario
    private Double cantidadEntregada;
    private Double totalOroRecaudado;

    // Beneficiario
    private String beneficiarioId; // Puede ser null en subsidios
    private String beneficiarioNombre;
    private String observaciones; // Obligatorio en subsidios

    private LocalDateTime fechaVenta;
}