package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document; // Cambiar a @Entity si usas JPA/Postgres

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "dragueros")
public class Draguero {
    @Id
    private String id;

    private String nombres;
    private String apellidos;
    private String cedula;
    private String telefono;
    private String ubicacionActividad;
    private Integer cantidadEquipos;
    private String observaciones;

    private LocalDateTime fechaRegistro;
}