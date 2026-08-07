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
@Document(collection = "minas")
public class Mina {

 @Id
 private String id;

 private String nombreMina;

 // Relación fundamental: ¿A qué sector pertenece esta mina?
 private String sectorMineroId;

 private String descripcion;

 @Builder.Default
 private boolean activa = true;
}