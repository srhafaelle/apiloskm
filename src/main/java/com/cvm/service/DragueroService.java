package com.cvm.service;
import com.cvm.dto.DragueroRequest;
import com.cvm.model.Draguero;

import java.util.List;

public interface DragueroService {
    Draguero registrarDraguero(DragueroRequest request);
    List<Draguero> obtenerTodos();
    List<Draguero> buscarPorFiltro(String filtro);
    Draguero obtenerPorId(String id);
    //comentario dde cambio
}