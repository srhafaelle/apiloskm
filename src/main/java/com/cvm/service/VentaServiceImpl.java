package com.cvm.service;

import com.cvm.dto.VentaRequest;
import com.cvm.model.*;
import com.cvm.repository.ProductoRepository;
import com.cvm.repository.PuntoDistribucionRepository;
import com.cvm.repository.TurnoRepository;
import com.cvm.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final PuntoDistribucionRepository puntoDistribucionRepository;
    private final TurnoRepository turnoRepository;

    @Override
    @Transactional
    public Venta procesarVenta(VentaRequest request, String emailCajero) {

        // 1. VALIDACIÓN DEL TURNO
        Turno turnoActivo = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("Error: Debe abrir un turno de caja antes de procesar ventas."));

        if (!turnoActivo.getPuntoDistribucionId().equals(request.getPuntoDistribucionId())) {
            throw new RuntimeException("Error de seguridad: Está intentando despachar desde un almacén distinto al de su turno activo.");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        PuntoDistribucion centro = puntoDistribucionRepository.findById(request.getPuntoDistribucionId())
                .orElseThrow(() -> new RuntimeException("Punto de distribución no encontrado"));

        // 2. VALIDACIÓN DE INVENTARIO (Nueva Arquitectura)
        // Ya no miramos solo el físico, miramos el Disponible (Físico - Comprometido)
        if (producto.getStockDisponible() < request.getCantidadSolicitada()) {
            throw new RuntimeException("Stock insuficiente. Disponible global: " + producto.getStockDisponible() + " " + producto.getUnidad());
        }

        // 3. COMPROMETER EL INVENTARIO (No restamos el físico aún)
        double stockComprometidoActual = producto.getStockComprometido() != null ? producto.getStockComprometido() : 0.0;
        producto.setStockComprometido(stockComprometidoActual + request.getCantidadSolicitada());

        // Calculamos el costo
        Double costoTotalOro = 0.0;
        if (request.getTipoVenta() != TipoVenta.SUBSIDIO) {
            costoTotalOro = request.getMontoOro() != null ? request.getMontoOro() : (producto.getPrecioOro() * request.getCantidadSolicitada());

            Double recaudadoHistorico = producto.getOroRecaudadoHistorico() != null ? producto.getOroRecaudadoHistorico() : 0.0;
            producto.setOroRecaudadoHistorico(recaudadoHistorico + costoTotalOro);
        }

        productoRepository.save(producto);

        // 4. ACTUALIZAR ESTADÍSTICAS DEL TURNO (El cajero cuadra su caja con lo cobrado)
        if (request.getTipoVenta() != TipoVenta.CREDITO && request.getTipoVenta() != TipoVenta.SUBSIDIO) {
            turnoActivo.setTotalOroRecaudado(turnoActivo.getTotalOroRecaudado() + costoTotalOro);
        }

        turnoActivo.setCantidadOperaciones(turnoActivo.getCantidadOperaciones() + 1);

        boolean insumoEncontrado = false;
        for (Turno.ResumenInsumo resumen : turnoActivo.getResumenInsumos()) {
            if (resumen.getProductoId().equals(producto.getId())) {
                resumen.setTotalLitrosEntregados(resumen.getTotalLitrosEntregados() + request.getCantidadSolicitada());
                insumoEncontrado = true;
                break;
            }
        }
        if (!insumoEncontrado) {
            turnoActivo.getResumenInsumos().add(new Turno.ResumenInsumo(producto.getId(), producto.getNombre(), request.getCantidadSolicitada()));
        }
        turnoRepository.save(turnoActivo);

        // 5. CONSTRUIR Y GUARDAR LA VENTA PENDIENTE
        String numeroGuia = "GUI-" + System.currentTimeMillis();

        Venta nuevaVenta = Venta.builder()
                .turnoId(turnoActivo.getId())
                .numeroGuia(numeroGuia)
                .tipoVenta(request.getTipoVenta())
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .puntoDistribucionId(centro.getId())
                .nombreCentro(centro.getNombre())
                .usuarioCajeroId(emailCajero)

                // Nuevos campos de la arquitectura
                .cantidadSolicitada(request.getCantidadSolicitada())
                .cantidadEntregada(0.0) // Inicia en cero, no ha salido por la manguera
                .estado(EstadoVenta.PENDIENTE_ENTREGA)

                .montoTotalOro(costoTotalOro)
                .mineroId(request.getMineroId())
                .beneficiarioId(request.getBeneficiarioId())
                .beneficiarioNombre(request.getBeneficiarioNombre())
                .observaciones(request.getObservaciones())
                .fechaVenta(LocalDateTime.now())
                .pagada(request.getTipoVenta() != TipoVenta.CREDITO)
                .build();

        return ventaRepository.save(nuevaVenta);
    }

    // ========================================================================
    // NUEVO MÉTODO: ESTO LO LLAMARÁ FLUTTER (LA TABLET) AL LLENAR EL TAMBOR
    // ========================================================================
    @Transactional
    public Venta procesarDespachoFisico(String ventaId, Double cantidadDespachada) {
        Venta venta = findById(ventaId);

        if (venta.getEstado() == EstadoVenta.ENTREGADA) {
            throw new RuntimeException("Esta orden ya fue despachada en su totalidad.");
        }

        if (cantidadDespachada > venta.getCantidadPendiente()) {
            throw new RuntimeException("No puedes despachar más de lo pendiente (" + venta.getCantidadPendiente() + ")");
        }

        Producto producto = productoRepository.findById(venta.getProductoId()).orElseThrow();

        // 1. Restar del centro de distribución específico
        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(sc -> sc.getPuntoDistribucionId().equals(venta.getPuntoDistribucionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Centro no encontrado en el producto."));

        if (stockCentro.getCantidad() < cantidadDespachada) {
            throw new RuntimeException("El tanque físico no tiene suficiente combustible para este despacho.");
        }

        stockCentro.setCantidad(stockCentro.getCantidad() - cantidadDespachada);

        // 2. Liberar el stock comprometido
        producto.setStockComprometido(producto.getStockComprometido() - cantidadDespachada);

        // 3. Sumar a estadísticas históricas de despacho
        Double despachadoHistorico = producto.getCantidadTotalDespachada() != null ? producto.getCantidadTotalDespachada() : 0.0;
        producto.setCantidadTotalDespachada(despachadoHistorico + cantidadDespachada);

        // Recalcular Físico global
        producto.recalcularStockGlobal();
        productoRepository.save(producto);

        // 4. Actualizar estado de la venta
        double entregada = (venta.getCantidadEntregada() != null ? venta.getCantidadEntregada() : 0.0) + cantidadDespachada;
        venta.setCantidadEntregada(entregada);
        venta.setFechaUltimoDespacho(LocalDateTime.now());

        if (venta.getCantidadPendiente() == 0) {
            venta.setEstado(EstadoVenta.ENTREGADA);
        } else {
            venta.setEstado(EstadoVenta.ENTREGA_PARCIAL);
        }

        return ventaRepository.save(venta);
    }

    @Override
    public Venta findById(String id) {
        return ventaRepository.findById(id).orElseThrow(()->new RuntimeException("Venta no encontrada"));
    }

    @Override
    public List<Venta> findAll(){
        return ventaRepository.findAll();
    }

    @Override
    public List<Venta> findByBeneficiarioId(String beneficiarioId) {
        return ventaRepository.findByBeneficiarioIdOrderByFechaVentaDesc(beneficiarioId);
    }

    @Override
    public List<Venta> obtenerCreditosPendientes() {
        return ventaRepository.findByTipoVentaAndPagadaFalse(TipoVenta.CREDITO);
    }

    @Override
    @Transactional
    public Venta pagarCredito(String ventaId, String emailCajero) {
        Venta venta = findById(ventaId);

        if (venta.getTipoVenta() != TipoVenta.CREDITO) {
            throw new RuntimeException("Esta venta no es un crédito.");
        }
        if (venta.getPagada()) {
            throw new RuntimeException("Este crédito ya fue pagado.");
        }

        // Buscar el turno activo del cajero que está cobrando la deuda
        Turno turnoActivo = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("Error: Debe abrir un turno de caja para cobrar esta deuda."));

        // Sumar el oro al turno de caja actual
        turnoActivo.setTotalOroRecaudado(turnoActivo.getTotalOroRecaudado() + venta.getMontoTotalOro());
        turnoRepository.save(turnoActivo);

        // Marcar la deuda como pagada
        venta.setPagada(true);
        // Opcional: Podrías guardar el usuario que cobró la deuda
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> obtenerVentasPendientesDeDespacho() {
        // Retornamos tanto las vírgenes como las parcialmente despachadas
        return ventaRepository.findByEstadoIn(
                java.util.Arrays.asList(EstadoVenta.PENDIENTE_ENTREGA, EstadoVenta.ENTREGA_PARCIAL)
        );
    }
}