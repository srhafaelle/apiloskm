package com.cvm.service;

import com.cvm.dto.MineroRequest;
import com.cvm.model.BrigadaMinera;
import com.cvm.model.CuotaArrime;
import com.cvm.model.EstadoCuota;
import com.cvm.model.Minero;
import com.cvm.repository.BrigadaMineraRepository;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.cvm.model.PlanArrime;
import com.cvm.model.CuotaArrime;
import com.cvm.model.EstadoCuota;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MineroServiceImpl implements MineroService {

    private final MineroRepository mineroRepository;
    private final BrigadaMineraRepository brigadaMineraRepository;

    @Override
    public Minero createMinero(MineroRequest request) {
        if (mineroRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("Ya existe un minero registrado con la cédula: " + request.getCedula());
        }

        Minero minero = Minero.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cedula(request.getCedula())
                .cargo(request.getCargo())
                .esFundador(request.isEsFundador())
                .build();

        return mineroRepository.save(minero);
    }

    @Override
    public List<Minero> getAllMineros() {
        return mineroRepository.findAll();
    }

    @Override
    public Minero getMineroById(String id) {
        return mineroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado con el ID: " + id));
    }

    @Override
    public Minero updateMinero(String id, MineroRequest request) {
        Minero minero = mineroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado con el ID: " + id));

        minero.setNombres(request.getNombres());
        minero.setApellidos(request.getApellidos());
        minero.setCargo(request.getCargo());

        // Solo permitimos cambiar esFundador si no está actualmente en una brigada como fundador
        if (request.isEsFundador() != minero.isEsFundador()) {
            if (minero.getBrigadaActualId() != null) {
                BrigadaMinera brigada = brigadaMineraRepository.findById(minero.getBrigadaActualId()).orElse(null);
                if (brigada != null && brigada.getFundadoresIds().contains(minero.getId())) {
                    throw new RuntimeException("No se puede cambiar el rol de fundador mientras pertenezca a una brigada como fundador. Retírelo primero.");
                }
            }
            minero.setEsFundador(request.isEsFundador());
        }

        return mineroRepository.save(minero);
    }


    @Override
    public Minero registrarPagoInscripcion(String mineroId, Double montoOro) {
        Minero minero = getMineroById(mineroId);
        minero.setOroPagadoHastaLaFecha(minero.getOroPagadoHastaLaFecha() + montoOro);
        return mineroRepository.save(minero);
    }

    @Override
    public Minero asignarPlanArrime(String mineroId, Double cuotaMensual) {
        Minero minero = getMineroById(mineroId);

        if (!minero.inscripcionSolvente()) {
            throw new RuntimeException("El minero no puede iniciar un plan de arrime sin haber completado los 20g de inscripción.");
        }

        PlanArrime plan = PlanArrime.builder()
                .activo(true)
                .cuotaMensualAsignada(cuotaMensual)
                .fechaInicioPlan(LocalDate.now())
                .build();

        minero.setPlanArrime(plan);
        return mineroRepository.save(minero);
    }

    @Override
    public Minero registrarPagoArrime(String mineroId, Double montoOro) {
        Minero minero = getMineroById(mineroId);

        List<CuotaArrime> deudas = minero.obtenerMesesEnDeuda();
        if (deudas.isEmpty()) {
            throw new RuntimeException("El minero no tiene cuotas de arrime pendientes por pagar.");
        }

        Double oroRestante = montoOro;

        for (CuotaArrime cuota : deudas) {
            if (oroRestante <= 0) break;

            Double saldoPendiente = cuota.getSaldoPendiente();

            if (oroRestante >= saldoPendiente) {
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + saldoPendiente);
                cuota.setEstado(EstadoCuota.PAGADA);
                cuota.setFechaPagoCompletado(LocalDateTime.now());
                oroRestante -= saldoPendiente;
            } else {
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + oroRestante);
                oroRestante = 0.0;
            }
        }
        return mineroRepository.save(minero);
    }

    @Override
    public Minero togglePausaOperaciones(String mineroId) {
        Minero minero = getMineroById(mineroId);
        // Invierte el estado: Si estaba paralizado lo activa, si estaba activo lo paraliza
        minero.setOperacionesParalizadas(!minero.isOperacionesParalizadas());
        return mineroRepository.save(minero);
    }

    // Se ejecuta el día 1 de cada mes a medianoche
    @Override
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generarCuotasMensualesMineros() {
        List<Minero> mineros = mineroRepository.findAll();
        LocalDate hoy = LocalDate.now();
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String periodoActual = mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + hoy.getYear();

        for (Minero minero : mineros) {
            // Regla: Solo genera cuota si el plan está activo y NO ESTÁ PARALIZADO
            if (minero.getPlanArrime() != null && minero.getPlanArrime().isActivo() && !minero.isOperacionesParalizadas()) {

                minero.getHistorialCuotas().stream()
                        .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE && c.getFechaVencimiento().isBefore(hoy))
                        .forEach(c -> c.setEstado(EstadoCuota.VENCIDA));

                CuotaArrime nuevaCuota = CuotaArrime.builder()
                        .periodo(periodoActual)
                        .montoExigidoOro(minero.getPlanArrime().getCuotaMensualAsignada())
                        .fechaVencimiento(hoy.plusMonths(1).withDayOfMonth(1).minusDays(1))
                        .build();

                minero.getHistorialCuotas().add(nuevaCuota);
                mineroRepository.save(minero);
            }
        }
    }
}