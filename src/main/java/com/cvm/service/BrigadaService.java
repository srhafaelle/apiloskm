package com.cvm.service;

import com.cvm.dto.BrigadaRequest;
import com.cvm.dto.DashboardResponse;
import com.cvm.dto.DespachoRequest;
import com.cvm.dto.TesoreriaResponse;
import com.cvm.model.BrigadaMinera;

import java.time.LocalDate;
import java.util.List;

public interface BrigadaService {
    BrigadaMinera createBrigada(BrigadaRequest request);
    BrigadaMinera addMineroToBrigada(String brigadaId, String mineroId);
    List<BrigadaMinera> getAllBrigadas();
    BrigadaMinera registrarPagoInscripcion(String brigadaId, Double montoOro);
    BrigadaMinera registrarDespacho(String brigadaId, DespachoRequest request, String usuarioAdminEmail);
    BrigadaMinera asignarPlanArrime(String brigadaId, Double cuotaMensual);
    BrigadaMinera registrarPagoArrime(String brigadaId, Double montoOro);
    void generarCuotasMensuales(); // El proceso automático
    DashboardResponse obtenerMetricasDashboard();
    TesoreriaResponse obtenerTesoreriaPorFechas(LocalDate inicio, LocalDate fin);
}