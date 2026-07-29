package com.cvm.service;

import com.cvm.dto.PuntoDistribucionRequest;
import com.cvm.model.PuntoDistribucion;
import com.cvm.repository.PuntoDistribucionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PuntoDistribucionServiceImpl implements PuntoDistribucionService {
    //comentario dde cambio
    private final PuntoDistribucionRepository repository;

    @Override
    public PuntoDistribucion createPuntoDistribucion(PuntoDistribucionRequest request) {
        PuntoDistribucion punto = PuntoDistribucion.builder()
                .nombre(request.getNombre())
                .ubicacion(request.getUbicacion())
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return repository.save(punto);
    }

    @Override
    public List<PuntoDistribucion> getAllPuntos() {
        return repository.findAll();
    }

    @Override
    public PuntoDistribucion toggleActivo(String id) {
        PuntoDistribucion punto = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Punto de distribución no encontrado"));

        punto.setActivo(!punto.isActivo()); // Cambia de Activo a Inactivo y viceversa
        return repository.save(punto);
    }
}