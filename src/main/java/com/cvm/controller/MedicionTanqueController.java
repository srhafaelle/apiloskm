package com.cvm.controller;
import com.cvm.model.MedicionTanque;
import com.cvm.model.Tanque;
import com.cvm.service.IMedicionTanqueService;
import com.cvm.service.ITanqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tanques")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class MedicionTanqueController {

    @Autowired
    private IMedicionTanqueService medicionTanqueService;

    @Autowired
    private ITanqueService tanqueService;

    // --- ENDPOINTS DE CONFIGURACIÓN DE TANQUES ---
    @PostMapping("/configuracion")
    public ResponseEntity<?> crearTanque(@RequestBody Tanque tanque) {
        try {
            System.out.println("📥 JSON recibido: " + tanque);
            return ResponseEntity.ok(tanqueService.guardar(tanque));
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error exacto en tu consola de Java
            return ResponseEntity.badRequest().body("Error al guardar tanque: " + e.getMessage());
        }
    }

    @GetMapping("/configuracion")
    public ResponseEntity<?> listarTanques() {
        try {
            return ResponseEntity.ok(tanqueService.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al leer Mongo: " + e.getMessage());
        }
    }

    // --- ENDPOINTS DE MEDICIONES ---
    @PostMapping("/mediciones")
    public ResponseEntity<?> registrarMedicion(@RequestBody MedicionTanque medicion) {
        try {
            return ResponseEntity.ok(medicionTanqueService.guardar(medicion));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al guardar medición: " + e.getMessage());
        }
    }

    @GetMapping("/mediciones")
    public ResponseEntity<?> obtenerHistorial(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin") String fin) {
        try {
            return ResponseEntity.ok(medicionTanqueService.obtenerHistorial(inicio, fin));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar fechas: " + e.getMessage());
        }
    }
}