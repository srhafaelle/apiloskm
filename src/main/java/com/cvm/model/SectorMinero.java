package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sectores_mineros")
public class SectorMinero {

    @Id
    private String id;

    private String nombreDeSector;
    private String descripcion;
    private String encargadoId; // Solo guardamos el ID del responsable/encargado

    @Builder.Default
    private boolean activo = true;
}