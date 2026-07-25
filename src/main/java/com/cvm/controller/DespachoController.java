package com.cvm.controller;

import com.cvm.dto.DespachoPOSRequest;
import com.cvm.model.Despacho;
import com.cvm.service.DespachoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;

    @PostMapping
    public ResponseEntity<Despacho> procesarDespachoPOS(
            @Valid @RequestBody DespachoPOSRequest request,
            Principal principal) {

        String emailCajero = principal.getName();
        return ResponseEntity.ok(despachoService.registrarDespachoGlobal(request, emailCajero));
    }
}