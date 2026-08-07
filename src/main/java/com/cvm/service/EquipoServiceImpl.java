package com.cvm.service;

import com.cvm.dto.EquipoRequest;
import com.cvm.model.Equipo;
import com.cvm.model.Minero;
import com.cvm.model.TipoEquipo;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipoServiceImpl implements EquipoService {

    private final MineroRepository mineroRepository;

    @Override
    public List<String> obtenerCatalogoTipos() {
        // Convierte el Enum en una lista de Strings para mandarlo a Flutter
        return Arrays.stream(TipoEquipo.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public Minero agregarEquipoAMinero(String mineroId, EquipoRequest request) {
        Minero minero = mineroRepository.findById(mineroId)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado"));

        Equipo nuevoEquipo = Equipo.builder()
                .tipo(request.getTipo())
                .cantidad(request.getCantidad())
                .activo(request.isActivo())
                .sectorMineroId(request.getSectorMineroId())
                .build();

        minero.getEquipos().add(nuevoEquipo);
        return mineroRepository.save(minero);
    }

    @Override
    public Minero eliminarEquipoDeMinero(String mineroId, TipoEquipo tipo) {
        Minero minero = mineroRepository.findById(mineroId)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado"));

        // Remueve todos los equipos de ese tipo en este minero
        minero.getEquipos().removeIf(e -> e.getTipo() == tipo);
        return mineroRepository.save(minero);
    }
}