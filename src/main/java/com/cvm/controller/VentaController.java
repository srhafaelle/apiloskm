package com.cvm.controller;
import com.cvm.dto.VentaRequest;
import com.cvm.model.Venta;
import com.cvm.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping("/procesar")
    public ResponseEntity<Venta> procesarVenta(
            @Valid @RequestBody VentaRequest request,
            Principal principal) {

        // Identificamos al cajero que está haciendo la petición
        String emailCajero = (principal != null) ? principal.getName() : "CAJERO_SISTEMA";

        return ResponseEntity.ok(ventaService.procesarVenta(request, emailCajero));
    }

    @PostMapping
    public ResponseEntity<Venta> crearVenta(@Valid @RequestBody VentaRequest request,
                                            Authentication authentication) {
        String email = authentication.getName(); // el username es el correo
        Venta venta = ventaService.procesarVenta(request, email);
        return new ResponseEntity<>(venta, HttpStatus.CREATED);
    }

    // Endpoint para reimpresión de guía
    @GetMapping("/{id}")
    public Venta obtenerVentas(@PathVariable String id) {
        // Necesitarás inyectar VentaRepository y hacer findById, o exponer en el servicio.
        // Por ahora lo dejo como idea; implementa similar.

        return ventaService.findById(id);
    }


}