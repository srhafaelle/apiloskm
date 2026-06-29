package com.cvm.service;

import com.cvm.dto.MineroRequest;
import com.cvm.model.Minero;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MineroServiceImpl implements MineroService {

    private final MineroRepository mineroRepository;

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
}