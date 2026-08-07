package com.cvm.service;

import com.cvm.dto.EquipoRequest;
import com.cvm.model.Minero;
import com.cvm.model.TipoEquipo;
import java.util.List;

public interface EquipoService {
    List<String> obtenerCatalogoTipos();
    Minero agregarEquipoAMinero(String mineroId, EquipoRequest request);
    Minero eliminarEquipoDeMinero(String mineroId, TipoEquipo tipo);
}