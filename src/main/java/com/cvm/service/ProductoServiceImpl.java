package com.cvm.service;

import com.cvm.dto.ProductoRequest;
import com.cvm.model.Producto;
import com.cvm.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public Producto createProducto(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .unidad(request.getUnidad().toUpperCase())
                .precioOro(request.getPrecioOro())
                .activo(true)
                .stockDisponible(request.getStockDisponible())
                .cantidadTotalDespachada(request.getCantidadTotalDespachada())
                .oroRecaudadoHistorico(request.getOroRecaudadoHistorico())
                .build();

        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }
    @Override
    public Producto updateProducto(String id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioOro(request.getPrecioOro());
        producto.setUnidad(request.getUnidad());
        producto.setStockDisponible(request.getStockDisponible());

        return productoRepository.save(producto);
    }
}