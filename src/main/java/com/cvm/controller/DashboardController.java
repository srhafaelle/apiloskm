package com.cvm.controller;

import com.cvm.model.Producto;
import com.cvm.service.ProductoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

   
    private final ProductoService productoService;
    //comentario dde cambio

    @GetMapping("/productos")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Producto>> getEstadisticasProductos() {
        // Como el producto ya guarda sus propias métricas, solo necesitamos listarlos.
        // El frontend se encargará de hacer los gráficos con estos datos.
        return ResponseEntity.ok(productoService.getAllProductos());
    }

}