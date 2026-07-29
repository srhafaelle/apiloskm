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
@Document(collection = "recepciones_inventario")
public class RecepcionInventario {
    //comentario dde cambio
    @Id
    private String id;

    private String numeroFactura; // El código que pediste (ej. 123456856)
    private String productoId;
    private String nombreProducto;
    private String puntoDistribucionId;
    private String nombrePuntoDistribucion;

    private Double cantidadRecibida;
    private String observaciones; // Por si llegan menos litros o hay derrames

    private String usuarioReceptor; // Correo de quien registró la factura
    private LocalDateTime fechaRecepcion;
}