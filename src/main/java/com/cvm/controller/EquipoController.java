package com.cvm.controller;

import com.cvm.dto.EquipoRequest;
import com.cvm.model.Minero;
import com.cvm.model.TipoEquipo;
import com.cvm.service.EquipoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipos")
@RequiredArgsConstructor
public class EquipoController {

    private final EquipoService equipoService;

    // Endpoint clave para que Flutter llene el Dropdown
    @GetMapping("/tipos")
    public ResponseEntity<List<String>> listarTiposDeEquipos() {
        return ResponseEntity.ok(equipoService.obtenerCatalogoTipos());
    }

    @PostMapping("/minero/{mineroId}")
    public ResponseEntity<Minero> agregarEquipoAMinero(@PathVariable String mineroId, @RequestBody EquipoRequest request) {
        return ResponseEntity.ok(equipoService.agregarEquipoAMinero(mineroId, request));
    }

    @DeleteMapping("/minero/{mineroId}/{tipo}")
    public ResponseEntity<Minero> eliminarEquipoDeMinero(@PathVariable String mineroId, @PathVariable TipoEquipo tipo) {
        return ResponseEntity.ok(equipoService.eliminarEquipoDeMinero(mineroId, tipo));
    }
}