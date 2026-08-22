package com.cvm.service;

import com.cvm.dto.MineroDTO;
import com.cvm.dto.MineroRequest;
import com.cvm.model.Minero;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MineroService {

    Minero createMinero(MineroRequest request);
    List<Minero> getAllMineros();
    // 1. Para uso interno del backend (búsquedas y actualizaciones)
    Minero getMineroById(String id);

    // 2. NUEVO: Para enviar a Flutter con todos los cálculos listos
    MineroDTO obtenerPerfilMinero(String mineroId);
    Minero updateMinero(String id, MineroRequest request);

    List<MineroDTO> obtenerTodosLosPerfiles();

    Page<MineroDTO> obtenerTodosLosPerfiles(Pageable pageable, String search);


}