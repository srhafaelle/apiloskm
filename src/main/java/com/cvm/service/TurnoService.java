package com.cvm.service;
import com.cvm.dto.TurnoAperturaRequest;
import com.cvm.dto.TurnoResponse;

import java.util.List;

public interface TurnoService {
    TurnoResponse abrirTurno(TurnoAperturaRequest request, String emailCajero);
    TurnoResponse obtenerTurnoActivo(String emailCajero);
    TurnoResponse cerrarTurno(String emailCajero);
    List<TurnoResponse> obtenerTodosLosTurnos();//comentario dde cambio
}