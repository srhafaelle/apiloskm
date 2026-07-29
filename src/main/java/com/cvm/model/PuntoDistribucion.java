package com.cvm.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "puntos_distribucion")
public class PuntoDistribucion {
    //comentario dde cambio
    @Id
    private String id;

    private String nombre; // Ej: "Centro Principal", "Mina Los Pijiguaos"
    private String ubicacion;
    private boolean activo;

    private LocalDateTime fechaCreacion;
}