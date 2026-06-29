package com.cvm.controller;

import com.cvm.dto.BrigadaRequest;
import com.cvm.model.BrigadaMinera;
import com.cvm.service.BrigadaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cvm.dto.PagoRequest;
import com.cvm.dto.DespachoRequest;
import java.security.Principal;
import com.cvm.dto.PlanArrimeRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brigadas")
@RequiredArgsConstructor
public class BrigadaController {

    private final BrigadaService brigadaService;

    @PostMapping
    public ResponseEntity<BrigadaMinera> createBrigada(@Valid @RequestBody BrigadaRequest request) {
        return new ResponseEntity<>(brigadaService.createBrigada(request), HttpStatus.CREATED);
    }

    @PostMapping("/{brigadaId}/mineros/{mineroId}")
    public ResponseEntity<BrigadaMinera> addMinero(
            @PathVariable String brigadaId,
            @PathVariable String mineroId) {
        return ResponseEntity.ok(brigadaService.addMineroToBrigada(brigadaId, mineroId));
    }

    @GetMapping
    public ResponseEntity<List<BrigadaMinera>> getAllBrigadas() {
        return ResponseEntity.ok(brigadaService.getAllBrigadas());
    }
    @PostMapping("/{id}/pagos")
    public ResponseEntity<BrigadaMinera> registrarPagoInscripcion(
            @PathVariable String id,
            @Valid @RequestBody PagoRequest request) {

        BrigadaMinera brigadaActualizada = brigadaService.registrarPagoInscripcion(id, request.getMontoOro());
        return ResponseEntity.ok(brigadaActualizada);
    }

    @PostMapping("/{id}/despachos")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BrigadaMinera> registrarDespacho(
            @PathVariable String id,
            @Valid @RequestBody DespachoRequest request,
            Principal principal) {

        // principal.getName() nos da el 'subject' del JWT, que en nuestro caso es el Email del usuario
        String usuarioQueDespacha = principal.getName();

        BrigadaMinera brigadaActualizada = brigadaService.registrarDespacho(id, request, usuarioQueDespacha);
        return ResponseEntity.ok(brigadaActualizada);
    }

    @PostMapping("/{id}/plan-arrime")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BrigadaMinera> asignarPlan(
            @PathVariable String id,
            @Valid @RequestBody PlanArrimeRequest request) {
        return ResponseEntity.ok(brigadaService.asignarPlanArrime(id, request.getCuotaMensualAsignada()));
    }

    @PostMapping("/{id}/arrime/pagos")
  //  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BrigadaMinera> registrarPagoArrime(
            @PathVariable String id,
            @Valid @RequestBody PagoRequest request) { // Usamos PagoRequest (el de montoOro)
        return ResponseEntity.ok(brigadaService.registrarPagoArrime(id, request.getMontoOro()));
    }
}