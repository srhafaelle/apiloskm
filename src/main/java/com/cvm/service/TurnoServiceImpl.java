package com.cvm.service;
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



@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;
    private final VentaRepository ventaRepository;

    @Override
    public TurnoResponse abrirTurno(TurnoAperturaRequest request, String emailCajero) {
        Optional<Turno> turnoExistente = turnoRepository.findByUsuarioCajeroIdAndEstado(emailCajero, EstadoTurno.ABIERTO);
        if (turnoExistente.isPresent()) {
            throw new RuntimeException("Ya tienes un turno abierto en la estación: " + turnoExistente.get().getNombreCentro());
        }

        // 2. Crear el nuevo turno
        Turno nuevoTurno = Turno.builder()
                .usuarioCajeroId(emailCajero)
                .puntoDistribucionId(request.getPuntoDistribucionId())
                .nombreCentro(request.getNombreCentro())
                .estado(EstadoTurno.ABIERTO)
                .fechaApertura(LocalDateTime.now())
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

        // 3. Sellar el turno
        turno.setEstado(EstadoTurno.CERRADO);
        turno.setFechaCierre(LocalDateTime.now());
        Turno guardado = turnoRepository.save(turno);

        List<Venta> ventasDelTurno = ventaRepository.findByTurnoId(guardado.getId());
        return mapToResponse(guardado, ventasDelTurno);
    }

    // Método utilitario para convertir la Entidad en un DTO limpio para el frontend
    private TurnoResponse mapToResponse(Turno turno, List<Venta>ventas) {
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
}