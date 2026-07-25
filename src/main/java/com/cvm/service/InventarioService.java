package com.cvm.service;
import com.cvm.dto.VentaRequest;
import com.cvm.model.EstadoVenta;
import com.cvm.model.Producto;
import com.cvm.model.Venta;
import com.cvm.repository.ProductoRepository;
import com.cvm.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public InventarioService(ProductoRepository productoRepository, VentaRepository ventaRepository) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    /**
     * FASE 1: Se ejecuta desde el POS (Caja).
     * Cobra el dinero y compromete el inventario, pero no toca el físico.
     */
    @Transactional
    public Venta procesarVentaEnCaja(VentaRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 1. Validar disponibilidad real
        if (producto.getStockDisponible() < request.getCantidadSolicitada()) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStockDisponible());
        }

        // 2. Comprometer el inventario
        double stockComprometidoActual = producto.getStockComprometido() != null ? producto.getStockComprometido() : 0.0;
        producto.setStockComprometido(stockComprometidoActual + request.getCantidadSolicitada());
        productoRepository.save(producto);

        // 3. Generar la orden pendiente
        Venta venta = new Venta();
        venta.setMineroId(request.getMineroId());
        venta.setProductoId(producto.getId());
        venta.setCantidadSolicitada(request.getCantidadSolicitada());
        venta.setCantidadEntregada(0.0);
        venta.setMontoTotalOro(request.getMontoOro());
        venta.setTipoVenta(request.getTipoVenta());
        venta.setEstado(EstadoVenta.PENDIENTE_ENTREGA);
        venta.setFechaVenta(LocalDateTime.now());

        // Si es CREDITO, aquí también llamarías a tu servicio de CuentasPorCobrar

        return ventaRepository.save(venta);
    }

    /**
     * FASE 2: Se ejecuta desde la Tablet en Flutter (Patio de tanques).
     * Libera la deuda del stock comprometido y descuenta el stock físico.
     */
    @Transactional
    public Venta procesarDespachoFisico(String ventaId, Double cantidadADespachar) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ENTREGADA) {
            throw new RuntimeException("Esta orden ya fue despachada en su totalidad.");
        }

        if (cantidadADespachar > venta.getCantidadPendiente()) {
            throw new RuntimeException("No puedes despachar más de la cantidad pendiente (" + venta.getCantidadPendiente() + ")");
        }

        Producto producto = productoRepository.findById(venta.getProductoId()).orElseThrow();

        // 1. Restar del físico (el líquido sale del tanque)
        producto.setStockFisico(producto.getStockFisico() - cantidadADespachar);

        // 2. Restar del comprometido (la deuda con el minero se salda)
        producto.setStockComprometido(producto.getStockComprometido() - cantidadADespachar);

        productoRepository.save(producto);

        // 3. Actualizar la orden de venta
        double nuevaCantidadEntregada = (venta.getCantidadEntregada() != null ? venta.getCantidadEntregada() : 0.0) + cantidadADespachar;
        venta.setCantidadEntregada(nuevaCantidadEntregada);
        venta.setFechaUltimoDespacho(LocalDateTime.now());

        if (venta.getCantidadPendiente() == 0) {
            venta.setEstado(EstadoVenta.ENTREGADA);
        } else {
            venta.setEstado(EstadoVenta.ENTREGA_PARCIAL);
        }

        return ventaRepository.save(venta);
    }
}