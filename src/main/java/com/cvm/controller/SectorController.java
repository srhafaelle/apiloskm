package com.cvm.controller;

import com.cvm.model.Mina;
import com.cvm.model.SectorMinero;
import com.cvm.service.SectorServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sectores")
@RequiredArgsConstructor
public class SectorController {

    private final SectorServiceImpl sectorService;

    @GetMapping
    public ResponseEntity<List<SectorMinero>> listarSectores() {
        return ResponseEntity.ok(sectorService.obtenerTodosLosSectores());
    }

    @PostMapping
    public ResponseEntity<SectorMinero> crearSector(@RequestBody SectorMinero sector) {
        return ResponseEntity.ok(sectorService.crearSector(sector));
    }

    @GetMapping("/{sectorId}/minas")
    public ResponseEntity<List<Mina>> listarMinasDelSector(@PathVariable String sectorId) {
        return ResponseEntity.ok(sectorService.obtenerMinasPorSector(sectorId));
    }

    @PostMapping("/minas")
    public ResponseEntity<Mina> crearMina(@RequestBody Mina mina) {
        return ResponseEntity.ok(sectorService.crearMina(mina));
    }
}