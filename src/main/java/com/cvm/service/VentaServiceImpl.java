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
    //comentario dde cambio
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final PuntoDistribucionRepository puntoDistribucionRepository;
    private final TurnoRepository turnoRepository;
    @Override
    @Transactional
    public Venta procesarVenta(VentaRequest request, String emailCajero) {
        // 1. Turno activo
        Turno turnoActivo = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("Debe abrir un turno antes de procesar ventas."));

        if (!turnoActivo.getPuntoDistribucionId().equals(request.getPuntoDistribucionId())) {
            throw new RuntimeException("El turno pertenece a otro centro de distribución.");
        }

        // 2. Producto y centro
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

        PuntoDistribucion centro = puntoDistribucionRepository.findById(request.getPuntoDistribucionId())
                .orElseThrow(() -> new RuntimeException("Punto de distribución no encontrado."));

        // 3. Validación y descuento del inventario por centro
        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(sc -> sc.getPuntoDistribucionId().equals(centro.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no tiene inventario en " + centro.getNombre()));

        if (stockCentro.getCantidad() < request.getCantidadSolicitada()) {
            throw new RuntimeException("Stock insuficiente en " + centro.getNombre() +
                    ". Disponible: " + stockCentro.getCantidad() + " " + producto.getUnidad());
        }

        // Descontar la cantidad física del centro
        stockCentro.setCantidad(stockCentro.getCantidad() - request.getCantidadSolicitada());
        producto.recalcularStockGlobal();

        // ⚠️ ELIMINAMOS EL SAVE DE AQUÍ Y LO BAJAMOS

        // 4. Calcular costo (solo si no es subsidio)
        Double costoTotalOro = 0.0;
        if (request.getTipoVenta() != TipoVenta.SUBSIDIO) {
            costoTotalOro = request.getMontoOro() != null ? request.getMontoOro() :
                    producto.getPrecioOro() * request.getCantidadSolicitada();
            // Acumular histórico de oro recaudado
            Double historico = producto.getOroRecaudadoHistorico() != null ? producto.getOroRecaudadoHistorico() : 0.0;
            producto.setOroRecaudadoHistorico(historico + costoTotalOro);
        }

        // ✅ AQUÍ ES EL LUGAR CORRECTO PARA GUARDAR EL PRODUCTO (Ya tiene el stock restado y el oro sumado)
        productoRepository.save(producto);

        // 5. Actualizar turno (estadísticas de caja)
        if (request.getTipoVenta() != TipoVenta.CREDITO && request.getTipoVenta() != TipoVenta.SUBSIDIO) {
            turnoActivo.setTotalOroRecaudado(turnoActivo.getTotalOroRecaudado() + costoTotalOro);
        }
        turnoActivo.setCantidadOperaciones(turnoActivo.getCantidadOperaciones() + 1);

        // Actualizar resumen por insumo
        boolean insumoEncontrado = false;
        for (Turno.ResumenInsumo resumen : turnoActivo.getResumenInsumos()) {
            if (resumen.getProductoId().equals(producto.getId())) {
                resumen.setTotalLitrosEntregados(resumen.getTotalLitrosEntregados() + request.getCantidadSolicitada());
                insumoEncontrado = true;
                break;
            }
        }
        if (!insumoEncontrado) {
            turnoActivo.getResumenInsumos().add(new Turno.ResumenInsumo(
                    producto.getId(), producto.getNombre(), request.getCantidadSolicitada()));
        }
        turnoRepository.save(turnoActivo);

        // 6. Crear la venta (pendiente de entrega física)
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
                .cantidadSolicitada(request.getCantidadSolicitada())
                .cantidadEntregada(0.0)
                .estado(EstadoVenta.PENDIENTE_ENTREGA)
                .nombreChofer(request.getNombreChofer())
                .direccionDestino(request.getDireccionDestino())
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

        // Actualizar cantidad entregada y estado (sin modificar inventario)
        double nuevaEntregada = (venta.getCantidadEntregada() != null ? venta.getCantidadEntregada() : 0.0) + cantidadDespachada;
        venta.setCantidadEntregada(nuevaEntregada);
        venta.setFechaUltimoDespacho(LocalDateTime.now());
        venta.setEstado(venta.getCantidadPendiente() == 0 ? EstadoVenta.ENTREGADA : EstadoVenta.ENTREGA_PARCIAL);
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