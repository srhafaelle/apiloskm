package com.cvm.service;
import com.cvm.dto.AnalyticsResponse;
import java.time.LocalDate;

public interface AnalyticsService {
    AnalyticsResponse calcularMetricasGlobales(LocalDate fechaInicio, LocalDate fechaFin, String brigadaId, String mineroId);
}