package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mediciones_tanques")
public class MedicionTanque {
    @Id
    private String id;
    private String tanqueId;
    private String producto;
    private Double medidaCm;
    private Double volumenLitros;

    // --- BLINDADO: Usamos String para evitar cualquier error 400 de conversión ---
    private String fechaMedicion;

    private String usuario;
}