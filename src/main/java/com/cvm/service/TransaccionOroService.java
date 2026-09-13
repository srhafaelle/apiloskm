package com.cvm.service;

import com.cvm.model.TransaccionOro;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionOroService {
    List<TransaccionOro> obtenerMovimientosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    TransaccionOro registrarMovimiento(TransaccionOro transaccion, String usuarioResponsable);
}