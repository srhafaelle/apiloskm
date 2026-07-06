package com.cvm.service;
import com.cvm.dto.TurnoAperturaRequest;
import com.cvm.dto.TurnoResponse;

public interface TurnoService {
    TurnoResponse abrirTurno(TurnoAperturaRequest request, String emailCajero);
    TurnoResponse obtenerTurnoActivo(String emailCajero);
    TurnoResponse cerrarTurno(String emailCajero);
}