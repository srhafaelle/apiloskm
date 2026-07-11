package com.cvm.controller;
import com.cvm.dto.DragueroRequest;
import com.cvm.model.Draguero;
import com.cvm.service.DragueroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dragueros")
@RequiredArgsConstructor
public class DragueroController {

    private final DragueroService dragueroService;

    @PostMapping
    public ResponseEntity<Draguero> registrar(@RequestBody DragueroRequest request) {
        return ResponseEntity.ok(dragueroService.registrarDraguero(request));
    }

    @GetMapping
    public ResponseEntity<List<Draguero>> listarTodos(
            @RequestParam(required = false, name = "filtro") String filtro) {
        if (filtro != null && !filtro.isEmpty()) {
            return ResponseEntity.ok(dragueroService.buscarPorFiltro(filtro));
        }
        return ResponseEntity.ok(dragueroService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Draguero> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(dragueroService.obtenerPorId(id));
    }


}