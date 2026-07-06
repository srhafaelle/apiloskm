package com.cvm.controller;

import com.cvm.dto.MineroRequest;
import com.cvm.model.BrigadaMinera;
import com.cvm.model.Minero;
import com.cvm.service.BrigadaService;
import com.cvm.service.MineroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mineros")
@RequiredArgsConstructor
public class MineroController {

    private final MineroService mineroService;
    private final BrigadaService brigadaService;

    @PostMapping
    public ResponseEntity<Minero> createMinero(@Valid @RequestBody MineroRequest request) {
        return new ResponseEntity<>(mineroService.createMinero(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Minero>> getAllMineros() {
        return ResponseEntity.ok(mineroService.getAllMineros());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Minero> updateMinero(@PathVariable String id,
                                               @Valid @RequestBody MineroRequest request) {
        return ResponseEntity.ok(mineroService.updateMinero(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Minero> getMineroById(@PathVariable String id){
        return ResponseEntity.ok(mineroService.getMineroById(id));
    }


}