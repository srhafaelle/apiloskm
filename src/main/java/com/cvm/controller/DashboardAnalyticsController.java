package com.cvm.controller;
import com.cvm.dto.AnalyticsResponse;
import com.cvm.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class DashboardAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsResponse> getDashboardMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String brigadaId,
            @RequestParam(required = false) String mineroId) {

        // El servicio procesará de forma agregada las colecciones de Ventas, Despachos,
        // Productos y Cuotas de MongoDB aplicando los criterios de filtrado seleccionados.
        AnalyticsResponse response = analyticsService.calcularMetricasGlobales(fechaInicio, fechaFin, brigadaId, mineroId);

        return ResponseEntity.ok(response);
    }
}