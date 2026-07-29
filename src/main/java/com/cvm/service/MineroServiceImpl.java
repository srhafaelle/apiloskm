package com.cvm.service;
import com.cvm.dto.ArrimeTicketRequest;
import com.cvm.dto.MineroRequest;
import com.cvm.model.BrigadaMinera;
import com.cvm.model.CuotaArrime;
import com.cvm.model.EstadoCuota;
import com.cvm.model.Minero;
import com.cvm.model.TipoMinero;
import com.cvm.model.PlanArrime;
import com.cvm.repository.BrigadaMineraRepository;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
//comentario dde cambio
        Minero minero = Minero.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cedula(request.getCedula())
                .cargo(request.getCargo())
                .tipoMinero(request.getTipoMinero() != null ? request.getTipoMinero() : TipoMinero.TRABAJADOR)
                .cuotaInscripcionOro(request.getCuotaInscripcionOro()) // Dinámico
                .ubicacionTrabajo(request.getUbicacionTrabajo())
                .equipos(request.getEquipos())
                .build();

        minero.generarNumeroUnico();
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
        Minero minero = getMineroById(id);

        minero.setNombres(request.getNombres());
        minero.setApellidos(request.getApellidos());
        minero.setUbicacionTrabajo(request.getUbicacionTrabajo());
        minero.setEquipos(request.getEquipos());
        minero.setCargo(request.getCargo());

        // Manejo del cambio de Tipo de Minero
        if (request.getTipoMinero() != null && request.getTipoMinero() != minero.getTipoMinero()) {
            if (minero.getBrigadaActualId() != null && minero.getTipoMinero() == TipoMinero.JEFE_BRIGADA) {
                throw new RuntimeException("No puede degradar a un Jefe de Brigada mientras esté asignado a una. Retírelo primero.");
            }
            minero.setTipoMinero(request.getTipoMinero());
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

        // Ya no exige 20g estrictos, verifica contra lo que el admin le asignó
        if (!minero.inscripcionSolvente()) {
            throw new RuntimeException("El minero debe saldar su cuota de inscripción (" + minero.getCuotaInscripcionOro() + "g) antes de tener un plan mensual fijo.");
        }

        PlanArrime plan = PlanArrime.builder()
                .activo(true)
                .cuotaMensualAsignada(cuotaMensual)
                .fechaInicioPlan(LocalDate.now())
                .build();

        minero.setPlanArrime(plan);
        return mineroRepository.save(minero);
    }

    // EL NUEVO MÉTODO QUE RECIBE EL TICKET DEL CONTRALOR (OFFLINE-FIRST)
    public Minero procesarTicketArrime(String mineroId, ArrimeTicketRequest ticket, String contralorEmail) {
        Minero minero = getMineroById(mineroId);
        Double oroRestante = ticket.getMontoOro();

        // 1. Si el minero tiene deudas viejas (de su Plan de Arrime), las saldamos primero
        List<CuotaArrime> deudas = minero.obtenerMesesEnDeuda();

        for (CuotaArrime cuota : deudas) {
            if (oroRestante <= 0) break;

            Double saldoPendiente = cuota.getSaldoPendiente();

            if (oroRestante >= saldoPendiente) {
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + saldoPendiente);
                cuota.setEstado(EstadoCuota.PAGADA);
                cuota.setFechaPagoCompletado(LocalDateTime.now());
                cuota.setNumeroTicket(ticket.getNumeroTicket());
                cuota.setContralorEmailId(contralorEmail);
                cuota.setFechaCobroLocal(ticket.getFechaCobroLocal());

                oroRestante -= saldoPendiente;
            } else {
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + oroRestante);
                oroRestante = 0.0;
            }
        }

        // 2. Si sobró oro, o si el minero NO TENÍA DEUDAS ni plan, registramos un Cobro Espontáneo
        if (oroRestante > 0) {
            CuotaArrime abonoExtra = CuotaArrime.builder()
                    .periodo("Recaudación Campo - " + ticket.getFechaCobroLocal().toLocalDate().toString())
                    .tipoCobro(ticket.getTipoCobro() != null ? ticket.getTipoCobro() : "ESPONTANEO")
                    .montoExigidoOro(oroRestante)
                    .montoPagadoOro(oroRestante)
                    .estado(EstadoCuota.PAGADA)
                    .fechaPagoCompletado(LocalDateTime.now()) // Fecha de sincronización
                    .numeroTicket(ticket.getNumeroTicket())
                    .contralorEmailId(contralorEmail)
                    .fechaCobroLocal(ticket.getFechaCobroLocal()) // Fecha real de la tablet
                    .build();

            minero.getHistorialCuotas().add(abonoExtra);
        }

        return mineroRepository.save(minero);
    }

    // Mantenemos el antiguo para retrocompatibilidad rápida (opcional)
    @Override
    public Minero registrarPagoArrime(String mineroId, Double montoOro) {
        throw new RuntimeException("Método obsoleto. Use procesarTicketArrime que incluye auditoría de campo.");
    }

    @Override
    public Minero togglePausaOperaciones(String mineroId) {
        Minero minero = getMineroById(mineroId);
        minero.setOperacionesParalizadas(!minero.isOperacionesParalizadas());
        return mineroRepository.save(minero);
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generarCuotasMensualesMineros() {
        List<Minero> mineros = mineroRepository.findAll();
        LocalDate hoy = LocalDate.now();
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String periodoActual = mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + hoy.getYear();

        for (Minero minero : mineros) {
            if (minero.getPlanArrime() != null && minero.getPlanArrime().isActivo() && !minero.isOperacionesParalizadas()) {

                minero.getHistorialCuotas().stream()
                        .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE && c.getFechaVencimiento().isBefore(hoy))
                        .forEach(c -> c.setEstado(EstadoCuota.VENCIDA));

                CuotaArrime nuevaCuota = CuotaArrime.builder()
                        .periodo(periodoActual)
                        .tipoCobro("PLAN_MENSUAL")
                        .montoExigidoOro(minero.getPlanArrime().getCuotaMensualAsignada())
                        .fechaVencimiento(hoy.plusMonths(1).withDayOfMonth(1).minusDays(1))
                        .build();

                minero.getHistorialCuotas().add(nuevaCuota);
                mineroRepository.save(minero);
            }
        }
    }
}