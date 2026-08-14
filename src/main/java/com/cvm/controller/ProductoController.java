package com.cvm.controller;

import com.cvm.dto.IngresoStockRequest;
import com.cvm.dto.ProductoRequest;
import com.cvm.dto.ProductoStockResponse;
import com.cvm.model.CargaInsumo;
import com.cvm.model.Producto;
import com.cvm.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    //comentario dde cambio
    @PostMapping
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody ProductoRequest request) {
        return new ResponseEntity<>(productoService.createProducto(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos() {
        return ResponseEntity.ok(productoService.getAllProductos());
    }

    @PutMapping("/{id}")
   // @PreAuthorize("hasRole('ADMIN')") // Solo el ADMIN puede cambiar precios
    public ResponseEntity<Producto> updateProducto(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.updateProducto(id, request));
    }

    @PostMapping("/{id}/ingresar-stock")
    public ResponseEntity<Producto> ingresarStock(
            @PathVariable String id,
            @RequestBody IngresoStockRequest request, // Usamos el DTO
            java.security.Principal principal) {

        String usuarioReceptor = (principal != null) ? principal.getName() : "Usuario Desconocido";
        return ResponseEntity.ok(productoService.agregarStock(id, request, usuarioReceptor));
    }
    // NUEVO: Transferir stock entre centros
    @PostMapping("/{id}/transferir-stock")
    public ResponseEntity<Producto> transferirStock(
            @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        String origenId = (String) payload.get("origenId");
        String destinoId = (String) payload.get("destinoId");
        String nombreDestino = (String) payload.get("nombreDestino");
        Double cantidad = Double.valueOf(payload.get("cantidad").toString());
        return ResponseEntity.ok(productoService.transferirStock(id, origenId, destinoId, nombreDestino, cantidad));
    }

    // En ProductoController.java
    @GetMapping("/punto/{puntoId}/stock")
    public ResponseEntity<List<ProductoStockResponse>> getStockPorPunto(@PathVariable String puntoId) {
        List<ProductoStockResponse> result = productoService.getStockPorPunto(puntoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/cargas")
    public ResponseEntity<List<CargaInsumo>> getHistorialCargas(@PathVariable String id) {
        return ResponseEntity.ok(productoService.getHistorialCargas(id));
    }
}