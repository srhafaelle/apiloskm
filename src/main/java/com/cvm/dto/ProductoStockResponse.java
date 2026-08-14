package com.cvm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoStockResponse {
    private String productoId;
    private String nombreProducto;
    private double stockActual;
}