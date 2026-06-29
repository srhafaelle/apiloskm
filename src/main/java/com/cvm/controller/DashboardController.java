package com.cvm.controller;

import com.cvm.dto.DashboardResponse;
import com.cvm.dto.TesoreriaResponse;
import com.cvm.model.Producto;
import com.cvm.service.BrigadaService;
import com.cvm.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BrigadaService brigadaService;
    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getMetricas() {
        return ResponseEntity.ok(brigadaService.obtenerMetricasDashboard());
    }

    @GetMapping("/productos")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Producto>> getEstadisticasProductos() {
        // Como el producto ya guarda sus propias métricas, solo necesitamos listarlos.
        // El frontend se encargará de hacer los gráficos con estos datos.
        return ResponseEntity.ok(productoService.getAllProductos());
    }
    // NUEVO ENDPOINT PARA LA TESORERÍA
    @GetMapping("/tesoreria")
   // @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TesoreriaResponse> getTesoreriaPorFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        return ResponseEntity.ok(brigadaService.obtenerTesoreriaPorFechas(inicio, fin));
    }
}