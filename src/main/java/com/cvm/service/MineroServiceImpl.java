package com.cvm.service;

import com.cvm.dto.MineroRequest;
import com.cvm.model.BrigadaMinera;
import com.cvm.model.Minero;
import com.cvm.repository.BrigadaMineraRepository;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MineroServiceImpl implements MineroService {

    private final MineroRepository mineroRepository;
    private final BrigadaMineraRepository brigadaMineraRepository;

    @Override
    public Minero createMinero(MineroRequest request) {
        if (mineroRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("Ya existe un minero registrado con la cédula: " + request.getCedula());
        }

        Minero minero = Minero.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cedula(request.getCedula())
                .cargo(request.getCargo())
                .esFundador(request.isEsFundador())
                .build();

        return mineroRepository.save(minero);
    }

    @Override
    public List<Minero> getAllMineros() {
        return mineroRepository.findAll();
    }

    @Override
    public Minero getMineroById(String id) {
        return mineroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado con el ID: " + id));
    }

    @Override
    public Minero updateMinero(String id, MineroRequest request) {
        Minero minero = mineroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado con el ID: " + id));

        minero.setNombres(request.getNombres());
        minero.setApellidos(request.getApellidos());
        minero.setCargo(request.getCargo());

        // Solo permitimos cambiar esFundador si no está actualmente en una brigada como fundador
        if (request.isEsFundador() != minero.isEsFundador()) {
            if (minero.getBrigadaActualId() != null) {
                BrigadaMinera brigada = brigadaMineraRepository.findById(minero.getBrigadaActualId()).orElse(null);
                if (brigada != null && brigada.getFundadoresIds().contains(minero.getId())) {
                    throw new RuntimeException("No se puede cambiar el rol de fundador mientras pertenezca a una brigada como fundador. Retírelo primero.");
                }
            }
            minero.setEsFundador(request.isEsFundador());
        }

        return mineroRepository.save(minero);
    }
}