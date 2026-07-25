package com.cvm.controller;
import com.cvm.dto.DespachoFisicoRequest;
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
import java.util.List;

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

    @GetMapping("/all")
    public List<Venta> obtenerVentas(){

          return ventaService.findAll();
    }


    @GetMapping("/minero/{beneficiarioId}")
    public ResponseEntity<List<Venta>> obtenerVentasPorMinero(@PathVariable String beneficiarioId) {
        return ResponseEntity.ok(ventaService.findByBeneficiarioId(beneficiarioId));
    }

    // GET: /api/ventas/creditos-pendientes
    @GetMapping("/creditos-pendientes")
    public ResponseEntity<List<Venta>> obtenerCreditosPendientes() {
        return ResponseEntity.ok(ventaService.obtenerCreditosPendientes());
    }

    // POST: /api/ventas/creditos/{id}/pagar
    @PostMapping("/creditos/{id}/pagar")
    public ResponseEntity<Venta> pagarCredito(@PathVariable String id, java.security.Principal principal) {
        // principal.getName() obtiene el email del cajero logueado mediante Spring Security
        Venta ventaPagada = ventaService.pagarCredito(id, principal.getName());
        return ResponseEntity.ok(ventaPagada);
    }

    @GetMapping("/pendientes-despacho")
    public ResponseEntity<List<Venta>> obtenerPendientesDespacho() {
        return ResponseEntity.ok(ventaService.obtenerVentasPendientesDeDespacho());
    }

    // POST: /api/ventas/{id}/despacho-fisico
    // La tablet usa esto al confirmar que llenó el tambor
    @PostMapping("/{id}/despacho-fisico")
    public ResponseEntity<Venta> procesarDespachoFisico(
            @PathVariable String id,
            @Valid @RequestBody DespachoFisicoRequest request) {

        Venta ventaActualizada = ventaService.procesarDespachoFisico(id, request.getCantidadDespachada());
        return ResponseEntity.ok(ventaActualizada);
    }
}