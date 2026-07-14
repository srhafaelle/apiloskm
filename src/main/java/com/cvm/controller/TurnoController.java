package com.cvm.controller;
import com.cvm.dto.TurnoAperturaRequest;
import com.cvm.dto.TurnoResponse;
import com.cvm.model.Turno;
import com.cvm.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping("/abrir")
    public ResponseEntity<TurnoResponse> abrirTurno(
            @Valid @RequestBody TurnoAperturaRequest request,
            Principal principal) {
        // En tu entorno real, principal.getName() trae el correo del cajero
        String emailCajero = (principal != null) ? principal.getName() : "CAJERO_SISTEMA";
        return ResponseEntity.ok(turnoService.abrirTurno(request, emailCajero));
    }

    @GetMapping("/activo")
    public ResponseEntity<TurnoResponse> obtenerTurnoActivo(Principal principal) {
        String emailCajero = (principal != null) ? principal.getName() : "CAJERO_SISTEMA";
        return ResponseEntity.ok(turnoService.obtenerTurnoActivo(emailCajero));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<TurnoResponse> cerrarTurno(Principal principal) {
        String emailCajero = (principal != null) ? principal.getName() : "CAJERO_SISTEMA";
        return ResponseEntity.ok(turnoService.cerrarTurno(emailCajero));
    }
    @GetMapping
    public ResponseEntity<List<TurnoResponse>> getAllTurnos() {
        List<TurnoResponse> lista = turnoService.obtenerTodosLosTurnos();
        return ResponseEntity.ok(lista);
    }
}
