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
@Document(collection = "cargas_insumos")
public class CargaInsumo {
    @Id
    private String id;
    private String numeroFactura; // El número de guía del camión
    private String productoId;
    private String nombreProducto;
    private String puntoDistribucionId;
    private String nombreCentro;
    private Double cantidadLitros;
    private String usuarioReceptor; // Quien recibió el camión
    private LocalDateTime fechaRecepcion;
    //comentario dde cambio
}