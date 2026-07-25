package com.cvm.service;

import com.cvm.dto.ProductoRequest;
import com.cvm.model.Producto;
import java.util.List;

public interface ProductoService {
    Producto createProducto(ProductoRequest request);
    List<Producto> getAllProductos();
    Producto updateProducto(String id, ProductoRequest request);
    Producto agregarStock(String productoId, String centroId, String nombreCentro, Double cantidad, String numeroFactura, String usuarioReceptor);
    Producto transferirStock(String productoId, String origenId, String destinoId, String nombreDestino, Double cantidad);
}