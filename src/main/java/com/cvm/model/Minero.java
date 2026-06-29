package com.cvm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mineros")
public class Minero {

    @Id
    private String id;

    private String nombres;
    private String apellidos;

    @Indexed(unique = true)
    private String cedula;

    private String cargo;

    // Si un minero puede estar sin brigada temporalmente, este campo puede ser null
    private String brigadaActualId;
}