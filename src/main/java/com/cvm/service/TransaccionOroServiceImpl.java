package com.cvm.service;
import com.cvm.model.TransaccionOro;
import com.cvm.repository.TransaccionOroRepository;
import com.cvm.service.TransaccionOroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransaccionOroServiceImpl implements TransaccionOroService {

    private final TransaccionOroRepository repository;

    @Override
    public List<TransaccionOro> obtenerMovimientosPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return repository.findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(fechaInicio, fechaFin);
    }

    @Override
    public TransaccionOro registrarMovimiento(TransaccionOro transaccion, String usuarioResponsable) {
        transaccion.setUsuarioResponsable(usuarioResponsable);

        if (transaccion.getFechaTransaccion() == null) {
            transaccion.setFechaTransaccion(LocalDateTime.now());
        }

        return repository.save(transaccion);
    }
}