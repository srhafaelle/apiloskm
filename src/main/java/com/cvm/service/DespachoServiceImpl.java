package com.cvm.service;

import com.cvm.dto.DespachoPOSRequest;
import com.cvm.model.*;
import com.cvm.repository.BrigadaMineraRepository;
import com.cvm.repository.DespachoRepository;
import com.cvm.repository.ProductoRepository;
import com.cvm.repository.PuntoDistribucionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DespachoServiceImpl implements DespachoService {

    private final DespachoRepository despachoRepository;
    private final ProductoRepository productoRepository;
    private final PuntoDistribucionRepository puntoDistribucionRepository;
    private final BrigadaMineraRepository brigadaRepository;

    @Override
    @Transactional
    public Despacho registrarDespachoGlobal(DespachoPOSRequest request, String emailCajero) {

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        PuntoDistribucion centro = puntoDistribucionRepository.findById(request.getPuntoDistribucionId())
                .orElseThrow(() -> new RuntimeException("Punto de distribución no seleccionado"));

        // 1. Validar y descontar stock del centro específico
        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(sc -> sc.getPuntoDistribucionId().equals(centro.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no posee stock configurado en este centro"));

        if (stockCentro.getCantidad() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente en " + centro.getNombre() + ". Disponibles: " + stockCentro.getCantidad());
        }

        // Descontar inventario local y recalcular global
        stockCentro.setCantidad(stockCentro.getCantidad() - request.getCantidad());
        producto.recalcularStockGlobal();

        // 2. Calcular costo basándose en el tipo de despacho
        Double costoTotal = 0.0;
        if (request.getTipoDespacho() != TipoDespacho.SUBSIDIO) {
            costoTotal = producto.getPrecioOro() * request.getCantidad();
            producto.setOroRecaudadoHistorico(producto.getOroRecaudadoHistorico() + costoTotal);
        }

        producto.setCantidadTotalDespachada(producto.getCantidadTotalDespachada() + request.getCantidad());
        productoRepository.save(producto);

        // 3. Si es de tipo BRIGADA, sincronizar su historial embebido por compatibilidad
        if (request.getTipoDespacho() == TipoDespacho.BRIGADA) {
            BrigadaMinera brigada = brigadaRepository.findById(request.getBeneficiarioId())
                    .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

            // Creamos la instancia embebida para la brigada
            Despacho despachoEmbebido = Despacho.builder()
                    .productoId(producto.getId())
                    .nombreProducto(producto.getNombre())
                    .cantidadEntregada(request.getCantidad())
                    .costoEnOro(costoTotal)
                    .fechaDespacho(LocalDateTime.now())
                    .despachadoPorUsuarioId(emailCajero)
                    .puntoDistribucionId(centro.getId())
                    .nombrePuntoDistribucion(centro.getNombre())
                    .build();

            brigada.getHistorialDespachos().add(despachoEmbebido);
            brigadaRepository.save(brigada);
        }

        // 4. Guardar registro en la bitácora global de despachos
        Despacho despachoGlobal = Despacho.builder()
                .tipoDespacho(request.getTipoDespacho())
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .cantidadEntregada(request.getCantidad())
                .costoEnOro(costoTotal)
                .puntoDistribucionId(centro.getId())
                .nombrePuntoDistribucion(centro.getNombre())
                .beneficiarioId(request.getBeneficiarioId())
                .beneficiarioNombre(request.getBeneficiarioNombre())
                .observaciones(request.getObservaciones())
                .despachadoPorUsuarioId(emailCajero)
                .fechaDespacho(LocalDateTime.now())
                .build();

        return despachoRepository.save(despachoGlobal);
    }
}