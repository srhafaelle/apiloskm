package com.cvm.controller;
import com.cvm.dto.PuntoDistribucionRequest;
import com.cvm.model.PuntoDistribucion;
import com.cvm.service.PuntoDistribucionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/puntos-distribucion")
@RequiredArgsConstructor
public class PuntoDistribucionController {
//comentario dde cambio
    private final PuntoDistribucionService service;

    @PostMapping
    public ResponseEntity<PuntoDistribucion> createPunto(@Valid @RequestBody PuntoDistribucionRequest request) {
        return new ResponseEntity<>(service.createPuntoDistribucion(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PuntoDistribucion>> getAllPuntos() {
        return ResponseEntity.ok(service.getAllPuntos());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<PuntoDistribucion> toggleActivo(@PathVariable String id) {
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}