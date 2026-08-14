package com.cvm.service;

import com.cvm.dto.IngresoStockRequest;
import com.cvm.dto.ProductoRequest;
import com.cvm.dto.ProductoStockResponse;
import com.cvm.model.CargaInsumo;
import com.cvm.model.Producto;
import java.util.List;

public interface ProductoService {
    //comentario dde cambio
    Producto createProducto(ProductoRequest request);
    List<Producto> getAllProductos();
    Producto updateProducto(String id, ProductoRequest request);
    Producto agregarStock(String productoId, IngresoStockRequest request, String usuarioReceptor);
    Producto transferirStock(String productoId, String origenId, String destinoId, String nombreDestino, Double cantidad);
    List<ProductoStockResponse> getStockPorPunto(String puntoId);
    List<CargaInsumo> getHistorialCargas(String productoId);
}