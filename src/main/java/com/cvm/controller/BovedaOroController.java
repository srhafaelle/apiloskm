package com.cvm.controller;
import com.cvm.model.TransaccionOro;
import com.cvm.service.TransaccionOroService;
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

    private final TransaccionOroService transaccionService;

    @GetMapping("/movimientos")
    public ResponseEntity<List<TransaccionOro>> obtenerMovimientos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        return ResponseEntity.ok(transaccionService.obtenerMovimientosPorFecha(fechaInicio, fechaFin));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<TransaccionOro> registrarMovimiento(
            @RequestBody TransaccionOro transaccion,
            Authentication authentication) {

        return ResponseEntity.ok(transaccionService.registrarMovimiento(transaccion, authentication.getName()));
    }
}