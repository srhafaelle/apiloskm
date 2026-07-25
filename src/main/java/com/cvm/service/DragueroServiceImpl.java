package com.cvm.service;
import com.cvm.dto.DragueroRequest;
import com.cvm.model.Draguero;
import com.cvm.repository.DragueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DragueroServiceImpl implements DragueroService {

    private final DragueroRepository dragueroRepository;

    @Override
    @Transactional
    public Draguero registrarDraguero(DragueroRequest request) {
        Optional<Draguero> existente = dragueroRepository.findByCedula(request.getCedula());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un draguero registrado con la cédula: " + request.getCedula());
        }

        Draguero nuevoDraguero = Draguero.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cedula(request.getCedula())
                .telefono(request.getTelefono())
                .ubicacionActividad(request.getUbicacionActividad())
                .cantidadEquipos(request.getCantidadEquipos())
                .observaciones(request.getObservaciones())
                .fechaRegistro(LocalDateTime.now())
                .build();

        return dragueroRepository.save(nuevoDraguero);
    }

    @Override
    public List<Draguero> obtenerTodos() {
        return dragueroRepository.findAll();
    }

    @Override
    public List<Draguero> buscarPorFiltro(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return obtenerTodos();
        }
        return dragueroRepository.findByNombresContainingIgnoreCaseOrCedulaContainingIgnoreCase(filtro, filtro);
    }

    @Override
    public Draguero obtenerPorId(String id) {
        return dragueroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Draguero no encontrado"));
    }
}