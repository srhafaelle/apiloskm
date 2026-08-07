package com.cvm.controller;
import com.cvm.dto.ArrimeSyncRequest;
import com.cvm.model.Arrime;
import com.cvm.service.ArrimeServiceImpl;
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

    private final ArrimeServiceImpl arrimeServiceImpl;


    @PostMapping("/cobrar")
    public ResponseEntity<?> registrarArrime(
            @RequestBody ArrimeSyncRequest request,
            Authentication authentication) {

        // Extraemos el correo del usuario logueado en la tablet
        String contralorEmail = authentication.getName();

        arrimeServiceImpl.procesarArrime(request, contralorEmail);

        // Retornamos un OK simple ya que Flutter ya maneja el estado visual
        return ResponseEntity.ok(Map.of("mensaje", "Ticket procesado y registrado correctamente."));
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<?> sincronizarArrimesOffline(
            @RequestBody List<ArrimeSyncRequest> requests,
            Authentication authentication) {

        String contralorEmail = authentication.getName();
        arrimeServiceImpl.procesarArrimesEnLote(requests, contralorEmail);
        return ResponseEntity.ok(Map.of("mensaje", "Sincronización completada exitosamente."));
    }

    @GetMapping()
    public ResponseEntity<List<Arrime>> allArrimes(){

        return ResponseEntity.ok(arrimeServiceImpl.allArrime());
    }

    @GetMapping("/reportes")
    public ResponseEntity<List<Arrime>> reporteArrimes(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        return ResponseEntity.ok(arrimeServiceImpl.obtenerReporteArrimes(fechaInicio, fechaFin));
    }

    @GetMapping("/ticket/{ticket}")
    public ResponseEntity<Arrime> arrimeTicket(@PathVariable String ticket) {


        Arrime arrimeEncontrado = arrimeServiceImpl.findByNumeroTicket(ticket);

        if (arrimeEncontrado != null) {
            return ResponseEntity.ok(arrimeEncontrado);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/minero/{mineroId}")
    public ResponseEntity<List<Arrime>> arrimesPorMinero(@PathVariable String mineroId) {
        return ResponseEntity.ok(arrimeServiceImpl.findByMineroId(mineroId));
    }

}
