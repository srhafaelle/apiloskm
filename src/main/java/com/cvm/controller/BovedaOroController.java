package com.cvm.controller;
import com.cvm.model.TransaccionOro;
import com.cvm.repository.TransaccionOroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boveda")
@RequiredArgsConstructor
public class BovedaOroController {

    private final TransaccionOroRepository repository;

    // 1. Obtener el libro mayor filtrado por fechas
    @GetMapping("/movimientos")
    public ResponseEntity<List<TransaccionOro>> obtenerMovimientos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        return ResponseEntity.ok(repository.findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(fechaInicio, fechaFin));
    }

    // 2. Registrar una nueva entrada o salida
    @PostMapping("/movimientos")
    public ResponseEntity<TransaccionOro> registrarMovimiento(
            @RequestBody TransaccionOro transaccion,
            Authentication authentication) {

        transaccion.setUsuarioResponsable(authentication.getName()); // Quién lo hace
        if (transaccion.getFechaTransaccion() == null) {
            transaccion.setFechaTransaccion(LocalDateTime.now());
        }

        return ResponseEntity.ok(repository.save(transaccion));
    }
}