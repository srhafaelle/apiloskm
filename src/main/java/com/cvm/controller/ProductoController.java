package com.cvm.controller;

import com.cvm.dto.ProductoRequest;
import com.cvm.model.Producto;
import com.cvm.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

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
}