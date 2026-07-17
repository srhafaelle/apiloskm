package com.cvm.service;
import com.cvm.dto.DespachoPOSRequest;
import com.cvm.model.*;
import com.cvm.repository.*;
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
    private final MineroRepository mineroRepository;

    @Override
    @Transactional
    public Despacho registrarDespachoGlobal(DespachoPOSRequest request, String emailCajero) {

        // 1. ================= VALIDACIÓN DE BENEFICIARIOS Y CANDADO =================
        Minero mineroComprador = null;
        BrigadaMinera brigadaCompradora = null;

        if (request.getTipoDespacho() == TipoDespacho.MINERO_APOYO) {
            mineroComprador = mineroRepository.findById(request.getBeneficiarioId())
                    .orElseThrow(() -> new RuntimeException("Minero no encontrado en la base de datos."));

            // ¡AQUÍ ESTÁ EL CANDADO EXACTO!
            if (!mineroComprador.puedeComprarInsumos()) {
                throw new RuntimeException("OPERACIÓN DENEGADA: El minero " + mineroComprador.getNombres() +
                        " está moroso (Debe la inscripción o tiene 2+ meses de arrime vencidos).");
            }
        } else if (request.getTipoDespacho() == TipoDespacho.BRIGADA) {
            brigadaCompradora = brigadaRepository.findById(request.getBeneficiarioId())
                    .orElseThrow(() -> new RuntimeException("Brigada no encontrada en la base de datos."));
        }

        // 2. ================= INVENTARIO Y COSTOS =================
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el inventario"));

        PuntoDistribucion centro = puntoDistribucionRepository.findById(request.getPuntoDistribucionId())
                .orElseThrow(() -> new RuntimeException("Punto de distribución no seleccionado"));

        Producto.StockCentro stockCentro = producto.getInventarioPorCentro().stream()
                .filter(sc -> sc.getPuntoDistribucionId().equals(centro.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no posee stock configurado en la estación: " + centro.getNombre()));

        if (stockCentro.getCantidad() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente en " + centro.getNombre() + ". Disponibles: " + stockCentro.getCantidad() + " " + producto.getUnidad());
        }

        // Descontar inventario local y recalcular global
        stockCentro.setCantidad(stockCentro.getCantidad() - request.getCantidad());
        producto.recalcularStockGlobal();

        // Calcular costo basándose en el tipo de despacho
        Double costoTotal = 0.0;
        if (request.getTipoDespacho() != TipoDespacho.SUBSIDIO) {
            costoTotal = producto.getPrecioOro() * request.getCantidad();
            producto.setOroRecaudadoHistorico(producto.getOroRecaudadoHistorico() + costoTotal);
        }



        producto.setCantidadTotalDespachada(producto.getCantidadTotalDespachada() + request.getCantidad());
        productoRepository.save(producto);

        // 3. ================= CREACIÓN DEL DESPACHO =================
        Despacho despachoGlobal = Despacho.builder()
                .tipoDespacho(request.getTipoDespacho())
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre()) // Llenado automático con el nombre real para el Frontend
                .cantidadEntregada(request.getCantidad())
                .costoEnOro(costoTotal)
                .puntoDistribucionId(centro.getId())
                .nombrePuntoDistribucion(centro.getNombre()) // Llenado automático
                .beneficiarioId(request.getBeneficiarioId())
                .beneficiarioNombre(request.getBeneficiarioNombre())
                .observaciones(request.getObservaciones())
                .despachadoPorUsuarioId(emailCajero)
                .fechaDespacho(LocalDateTime.now())
                .build();

        // 4. ================= GUARDAR HISTORIALES (EMBEBIDOS) =================

        if (request.getTipoDespacho() == TipoDespacho.MINERO_APOYO && mineroComprador != null) {
            mineroComprador.getHistorialDespachos().add(despachoGlobal);

            // NUEVO: Acumular combustible (Solo si la unidad del producto es LITROS)
            if ("LITROS".equalsIgnoreCase(producto.getUnidad())) {
                Double cicloActual = mineroComprador.getLitrosCompradosCicloActual() == null ? 0.0 : mineroComprador.getLitrosCompradosCicloActual();
                mineroComprador.setLitrosCompradosCicloActual(cicloActual + request.getCantidad());
            }

            mineroRepository.save(mineroComprador);
        }
        if (request.getTipoDespacho() == TipoDespacho.MINERO_APOYO && mineroComprador != null) {
            mineroComprador.getHistorialDespachos().add(despachoGlobal);
            mineroRepository.save(mineroComprador);
        }
        else if (request.getTipoDespacho() == TipoDespacho.BRIGADA && brigadaCompradora != null) {
            brigadaCompradora.getHistorialDespachos().add(despachoGlobal);
            brigadaRepository.save(brigadaCompradora);
        }

        // 5. ================= GUARDAR Y RETORNAR GLOBAL =================
        return despachoRepository.save(despachoGlobal);
    }
}