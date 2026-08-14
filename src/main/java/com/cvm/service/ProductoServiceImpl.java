package com.cvm.service;

import com.cvm.dto.IngresoStockRequest;
import com.cvm.dto.ProductoRequest;
import com.cvm.dto.ProductoStockResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    //comentario dde cambio
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
    public Producto agregarStock(String productoId, IngresoStockRequest request, String usuarioReceptor) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(c -> c.getPuntoDistribucionId().equals(request.getCentroId()))
                .findFirst()
                .orElse(null);

        // Agregamos al tanque físico del centro (usando litrajeRecibido)
        if (stockCentro != null) {
            stockCentro.setCantidad(stockCentro.getCantidad() + request.getLitrajeRecibido());
        } else {
            producto.getInventarioPorCentro().add(new Producto.StockCentro(
                    request.getCentroId(), request.getNombreCentro(), request.getLitrajeRecibido()));
        }

        // Recalculamos el global
        producto.recalcularStockGlobal();
        Producto productoGuardado = productoRepository.save(producto);

        // Registrar CargaInsumo con todos los datos logísticos
        CargaInsumo carga = CargaInsumo.builder()
                .productoId(productoId)
                .nombreProducto(productoGuardado.getNombre())
                .puntoDistribucionId(request.getCentroId())
                .nombreCentro(request.getNombreCentro())
                .numeroDeFactura(request.getNumeroDeFactura())
                .numeroDeControl(request.getNumeroDeControl())
                .chofer(request.getChofer())
                .idChofer(request.getIdChofer())
                .fechaDeRecepcion(request.getFechaDeRecepcion() != null ? request.getFechaDeRecepcion() : LocalDateTime.now())
                .fechaDeFacturacion(request.getFechaDeFacturacion())
                .litrajeDeFactura(request.getLitrajeDeFactura())
                .litrajeRecibido(request.getLitrajeRecibido())
                .observacion(request.getObservacion())
                .usuarioReceptor(usuarioReceptor)
                .build();

        cargaInsumoRepository.save(carga);

        return productoGuardado;
    }


    @Override
    public List<CargaInsumo> getHistorialCargas(String productoId) {
        return cargaInsumoRepository.findByProductoIdOrderByFechaDeRecepcionDesc(productoId);
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
    @Override
// Método en el servicio
    public List<ProductoStockResponse> getStockPorPunto(String puntoId) {
        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(p -> {
                    Producto.StockCentro centro = p.getInventarioPorCentro().stream()
                            .filter(c -> c.getPuntoDistribucionId().equals(puntoId))
                            .findFirst().orElse(null);
                    double stock = (centro != null) ? centro.getCantidad() : 0.0;
                    return new ProductoStockResponse(p.getId(), p.getNombre(), stock);
                })
                .collect(Collectors.toList());
    }
}