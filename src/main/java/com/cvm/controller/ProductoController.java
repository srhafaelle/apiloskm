package com.cvm.controller;

import com.cvm.dto.ProductoRequest;
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
            @RequestBody Map<String, Object> payload,
            java.security.Principal principal) {

        String centroId = (String) payload.get("centroId");
        String nombreCentro = (String) payload.get("nombreCentro");
        Double cantidad = Double.valueOf(payload.get("cantidad").toString());
        String numeroFactura = payload.get("numeroFactura").toString();

        // Capturamos quién recibe el camión (el usuario logueado)
        String usuarioReceptor = (principal != null) ? principal.getName() : "Usuario Desconocido";

        return ResponseEntity.ok(productoService.agregarStock(id, centroId, nombreCentro, cantidad, numeroFactura, usuarioReceptor));
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
}