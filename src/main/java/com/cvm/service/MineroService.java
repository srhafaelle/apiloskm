package com.cvm.service;

import com.cvm.dto.MineroRequest;
import com.cvm.model.Minero;
import java.util.List;

public interface MineroService {
    Minero createMinero(MineroRequest request);
    List<Minero> getAllMineros();
    Minero getMineroById(String id);
    Minero updateMinero(String id, MineroRequest request);


    // NUEVOS MÉTODOS FINANCIEROS Y OPERATIVOS
    Minero registrarPagoInscripcion(String mineroId, Double montoOro);
    Minero asignarPlanArrime(String mineroId, Double cuotaMensual);
    Minero registrarPagoArrime(String mineroId, Double montoOro);
    Minero togglePausaOperaciones(String mineroId);
    void generarCuotasMensualesMineros();
}