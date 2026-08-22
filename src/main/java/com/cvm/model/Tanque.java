package com.cvm.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tanques_configuracion")
public class Tanque {
    @Id
    private String id;
    private String nombre;
    private String tipoProducto;
    private Double diametroCm;
    private Double largoCm;
}