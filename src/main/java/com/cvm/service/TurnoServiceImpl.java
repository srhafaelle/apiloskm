package com.cvm.service;
import com.cvm.dto.ProductoStockResponse;
import com.cvm.dto.TurnoAperturaRequest;
import com.cvm.dto.TurnoResponse;
import com.cvm.model.EstadoTurno;
import com.cvm.model.Turno;
import com.cvm.model.Venta;
import com.cvm.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cvm.repository.VentaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final VentaRepository ventaRepository;

    private final ProductoService productoService;

    @Override
    public TurnoResponse abrirTurno(TurnoAperturaRequest request, String emailCajero) {
        Optional<Turno> turnoExistente = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO);
        if (turnoExistente.isPresent()) {
            throw new RuntimeException("Ya tienes un turno abierto en la estación: " + turnoExistente.get().getNombreCentro());
        }

        // 1. Mapear los productos recibidos en el request al resumen inicial del turno
        List<Turno.ResumenInsumo> resumenInicial = request.getProductos().stream()
                .map(p -> new Turno.ResumenInsumo(
                        p.getProductoId(),
                        p.getNombreProducto() != null ? p.getNombreProducto() : "Desconocido",
                        0.0, // totalLitrosEntregados arranca en 0
                        p.getStockInicial(), // Stock inicial viene de Flutter (Request)
                        0.0  // stockFinal aún no se conoce al abrir
                ))
                .collect(Collectors.toList());

        // 2. Crear el nuevo turno
        Turno nuevoTurno = Turno.builder()
                .usuarioCajeroId(emailCajero)
                .puntoDistribucionId(request.getPuntoDistribucionId())
                .nombreCentro(request.getNombreCentro())
                .estado(EstadoTurno.ABIERTO)
                .fechaApertura(LocalDateTime.now())
                .resumenInsumos(resumenInicial) // Asignamos el inventario inicial
                .build();

        Turno guardado = turnoRepository.save(nuevoTurno);
        return mapToResponse(guardado, List.of());
    }

    @Override
    public TurnoResponse obtenerTurnoActivo(String emailCajero) {
        Turno turno = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("No tienes ningún turno abierto actualmente."));
        List<Venta> ventasDelTurno = ventaRepository.findByTurnoId(turno.getId());
        return mapToResponse(turno, ventasDelTurno);
    }

    @Override
    public TurnoResponse cerrarTurno(String emailCajero) {
        Turno turno = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO)
                .orElseThrow(() -> new RuntimeException("No se encontró un turno abierto para cerrar."));

        // 1. Obtener el stock físico real actual de los tanques al momento de cerrar
        List<ProductoStockResponse> stocksAlCierre = productoService.getStockPorPunto(turno.getPuntoDistribucionId());

        // 2. Actualizar el stock final en el resumen del turno
        if (turno.getResumenInsumos() != null) {
            for (Turno.ResumenInsumo insumo : turno.getResumenInsumos()) {
                // Buscamos cuánto quedó de ese producto en la estación
                double stockFisicoReal = stocksAlCierre.stream()
                        .filter(s -> s.getProductoId().equals(insumo.getProductoId()))
                        .mapToDouble(ProductoStockResponse::getStockActual)
                        .findFirst()
                        .orElse(0.0);

                insumo.setStockFinal(stockFisicoReal);
            }
        }

        // 3. Sellar el turno
        turno.setEstado(EstadoTurno.CERRADO);
        turno.setFechaCierre(LocalDateTime.now());
        Turno guardado = turnoRepository.save(turno);

        List<Venta> ventasDelTurno = ventaRepository.findByTurnoId(guardado.getId());
        return mapToResponse(guardado, ventasDelTurno);
    }

    private TurnoResponse mapToResponse(Turno turno, List<Venta> ventas) {
        TurnoResponse response = new TurnoResponse();
        response.setId(turno.getId());
        response.setUsuarioCajeroId(turno.getUsuarioCajeroId());
        response.setNombreCentro(turno.getNombreCentro());
        response.setEstado(turno.getEstado());
        response.setFechaApertura(turno.getFechaApertura());
        response.setFechaCierre(turno.getFechaCierre());
        response.setTotalOroRecaudado(turno.getTotalOroRecaudado());
        response.setCantidadOperaciones(turno.getCantidadOperaciones());
        response.setPuntoDistribucionId(turno.getPuntoDistribucionId());
        response.setResumenInsumos(turno.getResumenInsumos());
        response.setVentas(ventas);
        return response;
    }

    @Override
    public List<TurnoResponse> obtenerTodosLosTurnos() {
        List<Turno> turnos = turnoRepository.findAll();
        return turnos.stream()
                .map(turno -> mapToResponse(turno, null))   // no se cargan ventas en el listado
                .collect(Collectors.toList());
    }
}