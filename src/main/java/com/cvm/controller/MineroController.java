package com.cvm.controller;
import com.cvm.dto.MineroDTO;
import com.cvm.dto.MineroRequest;
import com.cvm.model.Minero;
import com.cvm.service.MineroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mineros")
@RequiredArgsConstructor
public class MineroController {
    //comentario dde cambio
    private final MineroService mineroService;


    @PostMapping
    public ResponseEntity<Minero> createMinero(@Valid @RequestBody MineroRequest request) {
        return new ResponseEntity<>(mineroService.createMinero(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MineroDTO>> getAllMineros() {
        // AHORA DEVUELVE LOS PERFILES CALCULADOS CON LOS ARRIMES REALES
        return ResponseEntity.ok(mineroService.obtenerTodosLosPerfiles());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Minero> updateMinero(@PathVariable String id,
                                               @Valid @RequestBody MineroRequest request) {
        return ResponseEntity.ok(mineroService.updateMinero(id, request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MineroDTO> getMineroById(@PathVariable String id){
        return ResponseEntity.ok(mineroService.obtenerPerfilMinero(id));
    }



}