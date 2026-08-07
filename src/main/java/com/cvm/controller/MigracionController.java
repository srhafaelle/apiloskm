package com.cvm.controller;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/migracion")
@RequiredArgsConstructor
public class MigracionController {

    // MongoTemplate nos permite leer la base de datos sin pasar por los modelos,
    // evitando así que Spring Boot explote por los datos viejos.
    private final MongoTemplate mongoTemplate;

    @GetMapping("/mineros")
    public ResponseEntity<String> migrarEsquemaMineros() {

        // 1. Leemos TODOS los documentos tal cual están en la base de datos (Formato Crudo)
        List<Document> documentos = mongoTemplate.findAll(Document.class, "mineros");
        int actualizados = 0;

        for (Document doc : documentos) {
            boolean modificado = false;

            // --- REGLA 1: Arreglar 'equipos' (De String "" a Lista vacía []) ---
            Object equiposObj = doc.get("equipos");
            if (equiposObj instanceof String) {
                doc.put("equipos", new ArrayList<>()); // Lo convertimos en lista
                modificado = true;
            }

            // --- REGLA 2: Arreglar 'ubicacionTrabajo' (Pasar a sectorMineroIds) ---
            if (doc.containsKey("ubicacionTrabajo")) {
                doc.remove("ubicacionTrabajo"); // Borramos el viejo
                if (!doc.containsKey("sectorMineroIds")) {
                    doc.put("sectorMineroIds", new ArrayList<>()); // Creamos el nuevo
                }
                modificado = true;
            }

            // --- REGLA 3: Generar 'numeroUnicoRegistro' si no lo tienen ---
            if (!doc.containsKey("numeroUnicoRegistro") || doc.getString("numeroUnicoRegistro") == null) {
                String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String nuevoNumero = "MIN-" + uuidPart.substring(0, 4) + "-" + uuidPart.substring(4);
                doc.put("numeroUnicoRegistro", nuevoNumero);
                modificado = true;
            }

            // --- REGLA 4: Limpiar basura (Borrar campos que ya no existen en tu Java) ---
            String[] camposViejos = {
                    "historialCuotas", "historialArrimes", "historialDespachos",
                    "litrosCompradosCicloActual", "oroPagadoHastaLaFecha"
            };

            for (String campo : camposViejos) {
                if (doc.containsKey(campo)) {
                    doc.remove(campo);
                    modificado = true;
                }
            }

            // --- GUARDAR LOS CAMBIOS ---
            if (modificado) {
                mongoTemplate.save(doc, "mineros");
                actualizados++;
            }
        }

        return ResponseEntity.ok("Migración completada exitosamente. Mineros transformados: " + actualizados);
    }
}