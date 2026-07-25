package com.cvm.service;
import com.cvm.dto.AnalyticsResponse;
import com.cvm.dto.AnalyticsResponse.InsumoLitrosDetalle;
import com.cvm.dto.AnalyticsResponse.OperacionHistorialDTO;
import com.cvm.model.Producto;
import com.cvm.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final MongoTemplate mongoTemplate;

    @Override
    public AnalyticsResponse calcularMetricasGlobales(LocalDate fechaInicio, LocalDate fechaFin, String brigadaId, String mineroId) {
        // 1. Construir criterios de filtro comunes
        Criteria fechaCriteria = Criteria.where("fecha").gte(fechaInicio).lte(fechaFin);

        List<Criteria> andCriterias = new ArrayList<>();
        andCriterias.add(fechaCriteria);

        if (brigadaId != null && !brigadaId.trim().isEmpty()) {
            andCriterias.add(Criteria.where("brigadaId").is(brigadaId));
        }
        if (mineroId != null && !mineroId.trim().isEmpty()) {
            andCriterias.add(Criteria.where("mineroId").is(mineroId));
        }

        Criteria filtroComun = new Criteria().andOperator(andCriterias.toArray(new Criteria[0]));

        // 2. Obtener totales de cada tipo de operación
        Double totalVentasOro = obtenerTotal("ventas", filtroComun, "montoOro");
        Double totalArrimeOro = obtenerTotal("arrimes", filtroComun, "montoOro");
        Double totalInscripcionesOro = obtenerTotal("inscripciones", filtroComun, "montoOro");
        Double totalGeneral = totalVentasOro + totalArrimeOro + totalInscripcionesOro;

        // 3. Totales de litros vendidos e insumos
        Double totalLitrosVendidos = obtenerTotal("ventas", filtroComun, "litros");
        List<InsumoLitrosDetalle> detalleInsumos = obtenerDetalleLitrosPorProducto(filtroComun);

        // 4. Series de tiempo cruzadas
        Map<String, Double> comparativaDiaria = obtenerSerieTiempo("ventas", filtroComun, "%Y-%m-%d");
        Map<String, Double> comparativaSemanal = obtenerSerieTiempo("ventas", filtroComun, "%Y-%U");
        Map<String, Double> comparativaMensual = obtenerSerieTiempo("ventas", filtroComun, "%Y-%m");
        Map<String, Double> comparativaAnual = obtenerSerieTiempo("ventas", filtroComun, "%Y");

        // 5. Proyecciones analíticas basadas en el histórico
        Double proyeccionArrime = calcularProyeccionGenerica("arrimes", filtroComun, "montoOro");
        Double proyeccionLitros = calcularProyeccionGenerica("ventas", filtroComun, "litros");

        // 6. Historial unificado para la Bitácora
        List<OperacionHistorialDTO> historial = obtenerHistorialActividades(filtroComun);

        // 7. Construir DTO de respuesta consolidado
        return AnalyticsResponse.builder()
                .totalVentasOro(totalVentasOro)
                .totalArrimeOro(totalArrimeOro)
                .totalInscripcionesOro(totalInscripcionesOro)
                .totalGeneralBovedaOro(totalGeneral)
                .totalLitrosVendidos(totalLitrosVendidos)
                .totalLitrosDisponiblesStock(calcularStockTotalProductos())
                .detalleInsumosLitros(detalleInsumos)
                .comparativaDiaria(comparativaDiaria)
                .comparativaSemanal(comparativaSemanal)
                .comparativaMensual(comparativaMensual)
                .comparativaAnual(comparativaAnual)
                .proyeccionArrimeSiguienteMesOro(proyeccionArrime)
                .proyeccionConsumoLitrosSiguienteMes(proyeccionLitros)
                .historialActividades(historial)
                .build();
    }

    // ---------------------- Métodos Auxiliares de Agregación ----------------------

    private Double obtenerTotal(String collection, Criteria filtro, String field) {
        MatchOperation match = match(filtro);
        GroupOperation group = group().sum(field).as("total");
        Aggregation agg = newAggregation(match, group);
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, collection, Map.class);
        Map result = results.getUniqueMappedResult();
        if (result != null && result.get("total") != null) {
            return ((Number) result.get("total")).doubleValue();
        }
        return 0.0;
    }

    private List<InsumoLitrosDetalle> obtenerDetalleLitrosPorProducto(Criteria filtro) {
        MatchOperation match = match(filtro);
        GroupOperation groupByProducto = group("productoId").sum("litros").as("litrosVendidos");
        Aggregation aggVentas = newAggregation(match, groupByProducto);
        AggregationResults<Map> resultsVentas = mongoTemplate.aggregate(aggVentas, "ventas", Map.class);

        Map<String, Double> litrosVendidosPorProducto = new HashMap<>();
        for (Map map : resultsVentas.getMappedResults()) {
            String productoId = (String) map.get("_id");
            Number litros = (Number) map.get("litrosVendidos");
            if (productoId != null) {
                litrosVendidosPorProducto.put(productoId, litros != null ? litros.doubleValue() : 0.0);
            }
        }

        // Obtener los productos reales de tu base de datos
        List<Producto> productos = mongoTemplate.findAll(Producto.class, "productos");
        List<InsumoLitrosDetalle> detalle = new ArrayList<>();

        for (Producto p : productos) {
            Double vendidos = litrosVendidosPorProducto.getOrDefault(p.getId(), 0.0);
            // Mapeo con stockDisponible que usa tu modelo actual
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
        Aggregation agg = newAggregation(group().sum("stockDisponible").as("totalStock"));
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "productos", Map.class);
        Map result = results.getUniqueMappedResult();
        if (result != null && result.get("totalStock") != null) {
            return ((Number) result.get("totalStock")).doubleValue();
        }
        return 0.0;
    }

    private Map<String, Double> obtenerSerieTiempo(String collection, Criteria filtro, String dateFormat) {
        MatchOperation match = match(filtro);

        // Corrección de sintaxis nativa para formatear fechas en Spring Data MongoDB
        ProjectionOperation project = project()
                .and(DateOperators.DateToString.dateOf("fecha").toString(dateFormat)).as("periodo")
                .and("montoOro").as("monto");

        GroupOperation group = group("periodo").sum("monto").as("total");
        Aggregation agg = newAggregation(match, project, group, sort(Sort.by(Sort.Direction.ASC, "_id")));

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, collection, Map.class);
        Map<String, Double> mapa = new LinkedHashMap<>();

        for (Map map : results.getMappedResults()) {
            String periodo = (String) map.get("_id");
            Number total = (Number) map.get("total");
            if (periodo != null) {
                mapa.put(periodo, total != null ? total.doubleValue() : 0.0);
            }
        }
        return mapa;
    }

    private Double calcularProyeccionGenerica(String collection, Criteria filtroBase, String campo) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusMonths(3).withDayOfMonth(1);

        Criteria filtroUltimos3Meses = new Criteria().andOperator(
                filtroBase,
                Criteria.where("fecha").gte(inicio).lte(hoy)
        );

        Double totalTresMeses = obtenerTotal(collection, filtroUltimos3Meses, campo);
        return totalTresMeses / 3.0; // Promedio mensual predictivo básico
    }

    private List<OperacionHistorialDTO> obtenerHistorialActividades(Criteria filtroComun) {
        List<OperacionHistorialDTO> historial = new ArrayList<>();

        historial.addAll(obtenerHistorialPorTipo("ventas", filtroComun, "VENTA_INSUMO"));
        historial.addAll(obtenerHistorialPorTipo("arrimes", filtroComun, "ARRIME"));
        historial.addAll(obtenerHistorialPorTipo("inscripciones", filtroComun, "INSCRIPCION"));

        // Ordenar cronológicamente descendente
        historial.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        return historial.stream().limit(150).collect(Collectors.toList());
    }

    private List<OperacionHistorialDTO> obtenerHistorialPorTipo(String collection, Criteria filtro, String tipoOperacion) {
        MatchOperation match = match(filtro);
        ProjectionOperation project = project()
                .and(DateOperators.DateToString.dateOf("fecha").toString("%Y-%m-%d")).as("fecha")
                .and("montoOro").as("montoOro")
                .and("litros").as("volumenLitros")
                .and("brigadaId").as("brigadaId")
                .and("mineroId").as("mineroId")
                .and("descripcion").as("descripcion");

        Aggregation agg = newAggregation(match, project, sort(Sort.by(Sort.Direction.DESC, "fecha")));
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, collection, Map.class);

        List<OperacionHistorialDTO> lista = new ArrayList<>();
        for (Map map : results.getMappedResults()) {
            String brigadaId = (String) map.get("brigadaId");
            String mineroId = (String) map.get("mineroId");
            Number montoOro = (Number) map.get("montoOro");
            Number volumenLitros = (Number) map.get("volumenLitros");

            lista.add(OperacionHistorialDTO.builder()
                    .fecha((String) map.get("fecha"))
                    .entidadNombre(obtenerNombreEntidadReal(brigadaId, mineroId))
                    .tipoOperacion(tipoOperacion)
                    .descripcion((String) map.get("descripcion") != null ? (String) map.get("descripcion") : tipoOperacion)
                    .montoOro(montoOro != null ? montoOro.doubleValue() : 0.0)
                    .volumenLitros(volumenLitros != null ? volumenLitros.doubleValue() : 0.0)
                    .build());
        }
        return lista;
    }

    // Consulta en caliente los nombres reales desde sus respectivas colecciones
    private String obtenerNombreEntidadReal(String brigadaId, String mineroId) {
        if (mineroId != null && !mineroId.trim().isEmpty()) {
            Map minero = mongoTemplate.findOne(new Query(Criteria.where("_id").is(mineroId)), Map.class, "mineros");
            if (minero != null) {
                return minero.get("nombres") + " " + minero.get("apellidos");
            }
        } else if (brigadaId != null && !brigadaId.trim().isEmpty()) {
            Map brigada = mongoTemplate.findOne(new Query(Criteria.where("_id").is(brigadaId)), Map.class, "brigadas");
            if (brigada != null) {
                return (String) brigada.get("nombreBrigada");
            }
        }
        return "Fuerza Laboral CVM";
    }
}