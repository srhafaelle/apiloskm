package com.cvm.service;
import com.cvm.dto.VentaRequest;
import com.cvm.model.*;
import com.cvm.repository.ProductoRepository;
import com.cvm.repository.PuntoDistribucionRepository;
import com.cvm.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cvm.repository.TurnoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.cvm.model.*;


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

        // 1. VALIDACIÓN ESTRICTA: El cajero debe tener un turno abierto
        Turno turnoActivo = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("Error: Debe abrir un turno de caja antes de procesar ventas."));

        // Validar que no intente vender en un centro distinto al que abrió
        if (!turnoActivo.getPuntoDistribucionId().equals(request.getPuntoDistribucionId())) {
            throw new RuntimeException("Error de seguridad: Está intentando despachar desde un almacén distinto al de su turno activo.");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        PuntoDistribucion centro = puntoDistribucionRepository.findById(request.getPuntoDistribucionId())
                .orElseThrow(() -> new RuntimeException("Punto de distribución no encontrado"));

        Producto.StockCentro stockEnCentro = producto.getInventarioPorCentro().stream()
                .filter(sc -> sc.getPuntoDistribucionId().equals(centro.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Este producto no tiene inventario asignado en: " + centro.getNombre()));

        if (stockEnCentro.getCantidad() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente. El almacén " + centro.getNombre() + " solo tiene " + stockEnCentro.getCantidad() + " " + producto.getUnidad());
        }

        // Descontar Stock y Recalcular
        stockEnCentro.setCantidad(stockEnCentro.getCantidad() - request.getCantidad());
        producto.recalcularStockGlobal();

        Double costoTotalOro = 0.0;
        if (!"SUBSIDIO".equalsIgnoreCase(request.getTipoVenta())) {
            costoTotalOro = producto.getPrecioOro() * request.getCantidad();
            producto.setOroRecaudadoHistorico(producto.getOroRecaudadoHistorico() + costoTotalOro);
        }

        producto.setCantidadTotalDespachada(producto.getCantidadTotalDespachada() + request.getCantidad());
        productoRepository.save(producto);

        // 2. ACTUALIZAR LAS ESTADÍSTICAS DEL TURNO ACTIVO
        turnoActivo.setTotalOroRecaudado(turnoActivo.getTotalOroRecaudado() + costoTotalOro);
        turnoActivo.setCantidadOperaciones(turnoActivo.getCantidadOperaciones() + 1);

        // Buscar si el producto ya está en el resumen del turno para sumar los litros, si no, lo agregamos
        boolean insumoEncontrado = false;
        for (Turno.ResumenInsumo resumen : turnoActivo.getResumenInsumos()) {
            if (resumen.getProductoId().equals(producto.getId())) {
                resumen.setTotalLitrosEntregados(resumen.getTotalLitrosEntregados() + request.getCantidad());
                insumoEncontrado = true;
                break;
            }
        }
        if (!insumoEncontrado) {
            turnoActivo.getResumenInsumos().add(new Turno.ResumenInsumo(producto.getId(), producto.getNombre(), request.getCantidad()));
        }
        turnoRepository.save(turnoActivo); // Guardamos la foto actualizada del turno

        String numeroGuia = "GUI-" + System.currentTimeMillis();

        // 3. CONSTRUIR Y GUARDAR LA VENTA (Enlazada al turno)
        Venta nuevaVenta = Venta.builder()
                .turnoId(turnoActivo.getId()) // ENLACE CLAVE
                .numeroGuia(numeroGuia)
                .tipoVenta(request.getTipoVenta())
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .puntoDistribucionId(centro.getId())
                .nombreCentro(centro.getNombre())
                .usuarioCajeroId(emailCajero)
                .cantidadEntregada(request.getCantidad())
                .totalOroRecaudado(costoTotalOro)
                .beneficiarioId(request.getBeneficiarioId())
                .beneficiarioNombre(request.getBeneficiarioNombre())
                .observaciones(request.getObservaciones())
                .fechaVenta(LocalDateTime.now())
                .build();

        return ventaRepository.save(nuevaVenta);
    }

    @Override
    public Venta findById(String id) {
       return ventaRepository.findById(id).orElseThrow(()->new RuntimeException("venta no encontrada"));
    }

    @Override
    public List<Venta>findAll(){
        return   ventaRepository.findAll();
    }

    @Override
    public List<Venta> findByBeneficiarioId(String beneficiarioId) {
        return ventaRepository.findByBeneficiarioIdOrderByFechaVentaDesc(beneficiarioId);
    }
}