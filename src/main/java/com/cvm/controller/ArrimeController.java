package com.cvm.controller;
import com.cvm.dto.ArrimeSyncRequest;
import com.cvm.model.Arrime;
import com.cvm.service.ArrimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/arrimes")
@RequiredArgsConstructor
public class ArrimeController {

    private final ArrimeService arrimeService;

    // 1. Endpoint para cobrar un arrime individual (Cuando el Contralor tiene WiFi en campo)
    @PostMapping("/cobrar")
    public ResponseEntity<?> registrarArrime(
            @RequestBody ArrimeSyncRequest request,
            Authentication authentication) {

        // Extraemos el correo del usuario logueado en la tablet
        String contralorEmail = authentication.getName();

        arrimeService.procesarArrime(request, contralorEmail);

        // Retornamos un OK simple ya que Flutter ya maneja el estado visual
        return ResponseEntity.ok(Map.of("mensaje", "Ticket procesado y registrado correctamente en cuotas."));
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizarArrimesOffline(
            @RequestBody List<ArrimeSyncRequest> requests,
            Authentication authentication) {

        String contralorEmail = authentication.getName();

        arrimeService.procesarArrimesEnLote(requests, contralorEmail);
//comentario dde cambio

        return ResponseEntity.ok(Map.of("mensaje", "Sincronización completada exitosamente."));
    }
}
