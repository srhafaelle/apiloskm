package com.cvm.service;

import com.cvm.dto.ProductoRequest;
import com.cvm.model.CargaInsumo;
import com.cvm.model.Producto;
import com.cvm.repository.CargaInsumoRepository;
import com.cvm.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CargaInsumoRepository cargaInsumoRepository;

    @Override
    public Producto createProducto(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .unidad(request.getUnidad().toUpperCase())
                .precioOro(request.getPrecioOro())
                .activo(true)
                // Inicializamos todo en cero
                .stockFisico(0.0)
                .stockComprometido(0.0)
                .inventarioPorCentro(new ArrayList<>())
                .cantidadTotalDespachada(0.0)
                .oroRecaudadoHistorico(0.0)
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

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto agregarStock(String productoId, String centroId, String nombreCentro, Double cantidad,
                                 String numeroFactura, String usuarioReceptor) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(c -> c.getPuntoDistribucionId().equals(centroId))
                .findFirst()
                .orElse(null);

        // Agregamos al tanque físico del centro
        if (stockCentro != null) {
            stockCentro.setCantidad(stockCentro.getCantidad() + cantidad);
        } else {
            producto.getInventarioPorCentro().add(new Producto.StockCentro(centroId, nombreCentro, cantidad));
        }

        // Magia: Esto recalculará automáticamente el stockFisico global
        producto.recalcularStockGlobal();
        Producto productoGuardado = productoRepository.save(producto);

        // Registrar CargaInsumo
        CargaInsumo carga = CargaInsumo.builder()
                .numeroFactura(numeroFactura)
                .productoId(productoId)
                .nombreProducto(productoGuardado.getNombre())
                .puntoDistribucionId(centroId)
                .nombreCentro(nombreCentro)
                .cantidadLitros(cantidad)
                .usuarioReceptor(usuarioReceptor)
                .fechaRecepcion(LocalDateTime.now())
                .build();
        cargaInsumoRepository.save(carga);

        return productoGuardado;
    }

    @Override
    @Transactional
    public Producto transferirStock(String productoId, String origenId, String destinoId, String nombreDestino, Double cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Producto.StockCentro origen = producto.getInventarioPorCentro().stream()
                .filter(c -> c.getPuntoDistribucionId().equals(origenId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El centro de origen no tiene este producto."));

        if (origen.getCantidad() < cantidad) {
            throw new RuntimeException("Stock físico insuficiente en el centro de origen para transferir.");
        }

        Producto.StockCentro destino = producto.getInventarioPorCentro().stream()
                .filter(c -> c.getPuntoDistribucionId().equals(destinoId))
                .findFirst()
                .orElse(null);

        // Restamos del origen
        origen.setCantidad(origen.getCantidad() - cantidad);

        // Sumamos al destino
        if (destino != null) {
            destino.setCantidad(destino.getCantidad() + cantidad);
        } else {
            producto.getInventarioPorCentro().add(new Producto.StockCentro(destinoId, nombreDestino, cantidad));
        }

        // Recalculamos global
        producto.recalcularStockGlobal();
        return productoRepository.save(producto);
    }
}