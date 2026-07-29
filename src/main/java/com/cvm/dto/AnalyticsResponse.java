package com.cvm.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    // Totales de recaudación en Oro
    private Double totalVentasOro;
    private Double totalArrimeOro;
    private Double totalInscripcionesOro;
    private Double totalGeneralBovedaOro;

    // Nomenclatura específica de Combustibles/Líquidos (Litros)
    private Double totalLitrosVendidos;
    private Double totalLitrosDisponiblesStock;
    private List<InsumoLitrosDetalle> detalleInsumosLitros;
    //comentario dde cambio
    // Series de Tiempo Comparativas para Gráficos
    private Map<String, Double> comparativaDiaria;   // "YYYY-MM-DD" -> Monto Oro
    private Map<String, Double> comparativaSemanal;  // "Semana X" -> Monto Oro
    private Map<String, Double> comparativaMensual;  // "Mes X" -> Monto Oro
    private Map<String, Double> comparativaAnual;    // "YYYY" -> Monto Oro

    // Modelo de Proyección Basado en Tendencias Recientes
    private Double proyeccionArrimeSiguienteMesOro;
    private Double proyeccionConsumoLitrosSiguienteMes;

    // Historial para Tablas e Impresión de Reportes
    private List<OperacionHistorialDTO> historialActividades;

    @Data
    @Builder
    public static class InsumoLitrosDetalle {
        private String productoId;
        private String nombreInsumo;
        private Double litrosVendidosEnRango;
        private Double litrosRestantesEnStock;
    }

    @Data
    @Builder
    public static class OperacionHistorialDTO {
        private String fecha;
        private String entidadNombre; // Brigada o Minero involucrado
        private String tipoOperacion; // "VENTA_INSUMO", "ARRIME", "INSCRIPCION"
        private String descripcion;
        private Double montoOro;
        private Double volumenLitros; // Si aplica a combustibles
    }
}