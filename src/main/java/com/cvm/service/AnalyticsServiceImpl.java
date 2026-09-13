package com.cvm.service;
import com.cvm.dto.AnalyticsResponse;
import com.cvm.dto.AnalyticsResponse.InsumoLitrosDetalle;
import com.cvm.dto.AnalyticsResponse.OperacionHistorialDTO;
import com.cvm.model.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final MongoTemplate mongoTemplate;

    @Override
    public AnalyticsResponse calcularMetricasGlobales(LocalDate fechaInicio, LocalDate fechaFin, String brigadaId, String mineroId) {

        System.out.println("==================================================");
        System.out.println("🚀 [ANALYTICS] CALCULANDO MÉTRICAS GLOBALES");
        System.out.println("📅 Fechas: " + fechaInicio + " -> " + fechaFin);
        System.out.println("==================================================");

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // 1. OBTENER TOTALES (Cambiado "arrimes" por "arrimes_tickets")
        Double totalVentasOro = obtenerTotal("ventas", "fechaVenta", inicio, fin, "montoTotalOro", brigadaId, mineroId);
        Double totalArrimeOro = obtenerTotal("arrimes_tickets", "fechaCobroLocal", inicio, fin, "montoOro", brigadaId, mineroId);

        System.out.println("💰 [ORO] Total Ventas (montoTotalOro): " + totalVentasOro);
        System.out.println("💎 [ORO] Total Arrime (montoOro): " + totalArrimeOro);

        // Calcular Bóveda Real
        Double bovedaEntradas = obtenerTotalBoveda("ENTRADA", inicio, fin);
        Double bovedaSalidas = obtenerTotalBoveda("SALIDA", inicio, fin);
        Double totalGeneralBoveda = bovedaEntradas - bovedaSalidas;

        System.out.println("🏦 [BÓVEDA] Entradas: " + bovedaEntradas + " | Salidas: " + bovedaSalidas + " | Total Neto: " + totalGeneralBoveda);

        // 2. COMBUSTIBLES (Litros)
        Double totalLitrosVendidos = obtenerTotal("ventas", "fechaVenta", inicio, fin, "cantidadSolicitada", brigadaId, mineroId);
        List<InsumoLitrosDetalle> detalleInsumos = obtenerDetalleLitrosPorProducto(inicio, fin);

        System.out.println("⛽ [LITROS] Total Vendidos (cantidadSolicitada): " + totalLitrosVendidos);
        System.out.println("📦 [STOCK] Cantidad de Insumos mapeados: " + detalleInsumos.size());

        // 3. SERIES DE TIEMPO (Cambiado "arrimes" por "arrimes_tickets")
        Map<String, Double> comparativaDiaria = obtenerSerieTiempo("arrimes_tickets", "fechaCobroLocal", inicio, fin, "%Y-%m-%d", "montoOro");
        Map<String, Double> ventasDiarias = obtenerSerieTiempo("ventas", "fechaVenta", inicio, fin, "%Y-%m-%d", "cantidadSolicitada");
        Map<String, Double> recaudacionDiaria = obtenerSerieTiempo("ventas", "fechaVenta", inicio, fin, "%Y-%m-%d", "montoTotalOro");

        System.out.println("📈 [SERIES] Arrimes Diarios (Días con datos): " + comparativaDiaria.size());
        System.out.println("📈 [SERIES] Ventas Combustible Diarias: " + ventasDiarias.size());
        System.out.println("📈 [SERIES] Recaudación Combustible Diaria: " + recaudacionDiaria.size());

        // 4. HISTORIAL DE ACTIVIDADES
        List<OperacionHistorialDTO> historial = obtenerHistorialActividades(inicio, fin);
        System.out.println("📜 [HISTORIAL] Actividades encontradas en el periodo: " + historial.size());
        System.out.println("==================================================");

        return AnalyticsResponse.builder()
                .totalVentasOro(totalVentasOro)
                .totalArrimeOro(totalArrimeOro)
                .totalInscripcionesOro(0.0)
                .totalGeneralBovedaOro(totalGeneralBoveda)
                .totalLitrosVendidos(totalLitrosVendidos)
                .totalLitrosDisponiblesStock(calcularStockTotalProductos())
                .detalleInsumosLitros(detalleInsumos)
                .comparativaDiaria(comparativaDiaria)
                .ventasCombustibleDiarias(ventasDiarias)
                .recaudacionCombustibleDiaria(recaudacionDiaria)
                .historialActividades(historial)
                .proyeccionArrimeSiguienteMesOro(0.0)
                .proyeccionConsumoLitrosSiguienteMes(0.0)
                .build();
    }

    // ---------------------- MÉTODOS AUXILIARES ----------------------

    private Double obtenerTotal(String collection, String dateField, LocalDateTime inicio, LocalDateTime fin, String fieldToSum, String brigadaId, String mineroId) {
        List<Criteria> criterias = new ArrayList<>();
        criterias.add(Criteria.where(dateField).gte(inicio).lte(fin));

        if (brigadaId != null && !brigadaId.isEmpty()) criterias.add(Criteria.where("brigadaId").is(brigadaId));
        if (mineroId != null && !mineroId.isEmpty()) criterias.add(Criteria.where("mineroId").is(mineroId));

        MatchOperation match = match(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
        GroupOperation group = group().sum(fieldToSum).as("total");

        AggregationResults<Map> results = mongoTemplate.aggregate(newAggregation(match, group), collection, Map.class);
        Map result = results.getUniqueMappedResult();

        Double total = (result != null && result.get("total") != null) ? ((Number) result.get("total")).doubleValue() : 0.0;
        System.out.println("   -> [obtenerTotal] Colección: " + collection + " | Campo: " + fieldToSum + " | Total Calculado: " + total);
        return total;
    }

    private Double obtenerTotalBoveda(String tipoOperacion, LocalDateTime inicio, LocalDateTime fin) {
        Criteria criteria = new Criteria().andOperator(
                Criteria.where("fechaTransaccion").gte(inicio).lte(fin),
                Criteria.where("tipo").is(tipoOperacion)
        );
        MatchOperation match = match(criteria);
        GroupOperation group = group().sum("gramos").as("total");
        AggregationResults<Map> results = mongoTemplate.aggregate(newAggregation(match, group), "boveda_transacciones", Map.class);
        Map result = results.getUniqueMappedResult();
        return (result != null && result.get("total") != null) ? ((Number) result.get("total")).doubleValue() : 0.0;
    }

    private List<InsumoLitrosDetalle> obtenerDetalleLitrosPorProducto(LocalDateTime inicio, LocalDateTime fin) {
        MatchOperation match = match(Criteria.where("fechaVenta").gte(inicio).lte(fin));
        GroupOperation groupByProducto = group("productoId").sum("cantidadSolicitada").as("litrosVendidos");
        AggregationResults<Map> resultsVentas = mongoTemplate.aggregate(newAggregation(match, groupByProducto), "ventas", Map.class);

        Map<String, Double> litrosVendidosPorProducto = new HashMap<>();
        for (Map map : resultsVentas.getMappedResults()) {
            String productoId = (String) map.get("_id");
            Number litros = (Number) map.get("litrosVendidos");
            if (productoId != null) litrosVendidosPorProducto.put(productoId, litros != null ? litros.doubleValue() : 0.0);
        }

        List<Producto> productos = mongoTemplate.findAll(Producto.class, "productos");
        List<InsumoLitrosDetalle> detalle = new ArrayList<>();

        for (Producto p : productos) {
            Double vendidos = litrosVendidosPorProducto.getOrDefault(p.getId(), 0.0);
            Double stockActual = p.getStockDisponible() != null ? p.getStockDisponible() : 0.0;
            detalle.add(InsumoLitrosDetalle.builder()
                    .productoId(p.getId())
                    .nombreInsumo(p.getNombre())
                    .litrosVendidosEnRango(vendidos)
                    .litrosRestantesEnStock(stockActual)
                    .build());
        }
        return detalle;
    }

    private Double calcularStockTotalProductos() {
        AggregationResults<Map> results = mongoTemplate.aggregate(newAggregation(group().sum("stockDisponible").as("totalStock")), "productos", Map.class);
        Map result = results.getUniqueMappedResult();
        return (result != null && result.get("totalStock") != null) ? ((Number) result.get("totalStock")).doubleValue() : 0.0;
    }

    private Map<String, Double> obtenerSerieTiempo(String collection, String dateField, LocalDateTime inicio, LocalDateTime fin, String dateFormat, String campoASumar) {
        MatchOperation match = match(Criteria.where(dateField).gte(inicio).lte(fin));
        ProjectionOperation project = project()
                .and(DateOperators.DateToString.dateOf(dateField).toString(dateFormat)).as("periodo")
                .and(campoASumar).as("monto");

        GroupOperation group = group("periodo").sum("monto").as("total");
        Aggregation agg = newAggregation(match, project, group, sort(Sort.by(Sort.Direction.ASC, "_id")));

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, collection, Map.class);
        Map<String, Double> mapa = new LinkedHashMap<>();

        for (Map map : results.getMappedResults()) {
            String periodo = (String) map.get("_id");
            Number total = (Number) map.get("total");
            if (periodo != null) mapa.put(periodo, total != null ? total.doubleValue() : 0.0);
        }
        return mapa;
    }

    private List<OperacionHistorialDTO> obtenerHistorialActividades(LocalDateTime inicio, LocalDateTime fin) {
        List<OperacionHistorialDTO> historial = new ArrayList<>();
        historial.addAll(obtenerHistorialPorTipo("ventas", "fechaVenta", inicio, fin, "VENTA_INSUMO", "montoTotalOro", "cantidadSolicitada"));

        // (Cambiado "arrimes" por "arrimes_tickets")
        historial.addAll(obtenerHistorialPorTipo("arrimes_tickets", "fechaCobroLocal", inicio, fin, "ARRIME", "montoOro", null));

        // Ordenar cronológicamente descendente
        historial.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        return historial.stream().limit(100).collect(Collectors.toList());
    }

    private List<OperacionHistorialDTO> obtenerHistorialPorTipo(String collection, String dateField, LocalDateTime inicio, LocalDateTime fin, String tipoOperacion, String campoOro, String campoLitros) {
        MatchOperation match = match(Criteria.where(dateField).gte(inicio).lte(fin));

        ProjectionOperation project = project()
                .and(DateOperators.DateToString.dateOf(dateField).toString("%Y-%m-%d %H:%M")).as("fecha")
                .and(campoOro).as("montoOro")
                .and("brigadaId").as("brigadaId")
                .and("mineroId").as("mineroId")
                .and("descripcion").as("descripcion");

        if (campoLitros != null) {
            project = project.and(campoLitros).as("volumenLitros");
        }

        Aggregation agg = newAggregation(match, project, sort(Sort.by(Sort.Direction.DESC, "fecha")));
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, collection, Map.class);

        List<OperacionHistorialDTO> lista = new ArrayList<>();
        for (Map map : results.getMappedResults()) {
            Number montoOro = (Number) map.get("montoOro");
            Number volumenLitros = campoLitros != null ? (Number) map.get("volumenLitros") : 0.0;

            lista.add(OperacionHistorialDTO.builder()
                    .fecha((String) map.get("fecha"))
                    .entidadNombre(obtenerNombreEntidadReal((String) map.get("brigadaId"), (String) map.get("mineroId")))
                    .tipoOperacion(tipoOperacion)
                    .descripcion(map.containsKey("descripcion") ? (String) map.get("descripcion") : tipoOperacion)
                    .montoOro(montoOro != null ? montoOro.doubleValue() : 0.0)
                    .volumenLitros(volumenLitros != null ? volumenLitros.doubleValue() : 0.0)
                    .build());
        }
        return lista;
    }

    private String obtenerNombreEntidadReal(String brigadaId, String mineroId) {
        if (mineroId != null && !mineroId.isEmpty()) {
            Map minero = mongoTemplate.findOne(new Query(Criteria.where("_id").is(mineroId)), Map.class, "mineros");
            if (minero != null) return minero.get("nombres") + " " + minero.get("apellidos");
        }
        return "Operación CVM";
    }
}