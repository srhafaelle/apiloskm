package com.cvm.controller;
import com.cvm.dto.ArrimeTicketRequest;
import com.cvm.dto.MineroRequest;
import com.cvm.dto.PagoRequest;
import com.cvm.dto.PlanArrimeRequest;
import com.cvm.model.Minero;
import com.cvm.service.BrigadaService;
import com.cvm.service.MineroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @PostMapping("/{id}/pagos")
    public ResponseEntity<Minero> registrarPagoInscripcion(@PathVariable String id, @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(mineroService.registrarPagoInscripcion(id, request.getMontoOro()));
    }

    @PostMapping("/{id}/plan-arrime")
    public ResponseEntity<Minero> asignarPlan(@PathVariable String id, @Valid @RequestBody PlanArrimeRequest request) {
        return ResponseEntity.ok(mineroService.asignarPlanArrime(id, request.getCuotaMensualAsignada()));
    }

    // --- NUEVO ENDPOINT PARA RECIBIR EL TICKET DEL CONTRALOR (OFFLINE-FIRST) ---
    @PostMapping("/{id}/arrime/pagos")
    public ResponseEntity<Minero> procesarTicketArrime(
            @PathVariable String id,
            @Valid @RequestBody ArrimeTicketRequest request,
            Principal principal) {

        // Extraemos el correo o ID del JWT del Contralor que sincroniza
        String contralorEmail = principal != null ? principal.getName() : "SISTEMA";

        return ResponseEntity.ok(mineroService.procesarTicketArrime(id, request, contralorEmail));
    }

    @PutMapping("/{id}/paralizar")
    public ResponseEntity<Minero> togglePausaOperaciones(@PathVariable String id) {
        return ResponseEntity.ok(mineroService.togglePausaOperaciones(id));
    }
}