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

    private String productoId;
    private String nombreProducto;
    private String puntoDistribucionId;
    private String nombreCentro;
    private String usuarioReceptor;

    // --- NUEVOS CAMPOS LOGÍSTICOS ---
    private String numeroDeFactura;
    private String numeroDeControl;
    private String chofer;
    private String idChofer;
    private LocalDateTime fechaDeRecepcion;
    private LocalDateTime fechaDeFacturacion;
    private Double litrajeDeFactura;
    private Double litrajeRecibido;
    private String observacion;
}