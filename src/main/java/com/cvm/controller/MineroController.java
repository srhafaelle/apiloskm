package com.cvm.controller;

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

    private final MineroService mineroService;

    @PostMapping
    public ResponseEntity<Minero> createMinero(@Valid @RequestBody MineroRequest request) {
        return new ResponseEntity<>(mineroService.createMinero(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Minero>> getAllMineros() {
        return ResponseEntity.ok(mineroService.getAllMineros());
    }
}