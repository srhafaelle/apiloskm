package com.cvm.service;

import com.cvm.dto.ArrimeSyncRequest;
import com.cvm.model.Arrime;
import com.cvm.model.TipoDeArrime;
import com.cvm.repository.ArrimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArrimeServiceImpl implements ArrimeService {

    private final ArrimeRepository arrimeRepository;

    @Override
    @Transactional
    public void procesarArrime(ArrimeSyncRequest request, String contralorEmail) {

        LocalDateTime fechaOperacion = request.getFechaCobroLocal() != null ?
                request.getFechaCobroLocal() : LocalDateTime.now();

        // 1. Generamos el Número de Ticket Oficial del backend
        String numeroTicketOficial = generarNumeroTicket(fechaOperacion);

        // 2. LÓGICA DE NEGOCIO: Cálculo estricto del monto
        Double montoFinalOro = request.getMontoOro();

        if (request.getTipoDeArrime() == TipoDeArrime.DRAGA) {
            if (request.getProduccion() == null || request.getProduccion() <= 0) {
                throw new IllegalArgumentException("Para el cobro de DRAGA, la producción es obligatoria y mayor a 0.");
            }
            // El backend recalcula el 5% para evitar trampas
            montoFinalOro = request.getProduccion() * 0.05;
        }

        // 3. Construimos el registro
        Arrime nuevoArrime = Arrime.builder()
                .mineroId(request.getMineroId())
                .mineroNombre(request.getMineroNombre())
                .mineroCedula(request.getMineroCedula())
                .numeroSeguimiento(request.getNumeroSeguimiento()) // El de Flutter
                .numeroTicket(numeroTicketOficial) // El Oficial
                .sectorMineroId(request.getSectorMineroId())
                .tipoDeArrime(request.getTipoDeArrime())
                .produccion(request.getProduccion())
                .montoOro(montoFinalOro)
                .contralor(contralorEmail)
                .fechaCobroLocal(fechaOperacion)
                .build();

        arrimeRepository.save(nuevoArrime);
    }

    @Transactional
    public void procesarArrimesEnLote(List<ArrimeSyncRequest> requests, String contralorEmail) {
        log.info("Iniciando sincronización de {} arrimes offline", requests.size());
        for (ArrimeSyncRequest req : requests) {
            try {
                // IMPORTANTE: Validamos por número de seguimiento, porque el ticket aún no existe
                if (req.getNumeroSeguimiento() != null && arrimeRepository.existsByNumeroSeguimiento(req.getNumeroSeguimiento())) {
                    log.warn("El arrime offline {} ya fue sincronizado. Saltando...", req.getNumeroSeguimiento());
                    continue;
                }
                procesarArrime(req, contralorEmail);
            } catch (Exception e) {
                log.error("Error al sincronizar arrime {}: {}", req.getNumeroSeguimiento(), e.getMessage());
            }
        }
    }

    public List<Arrime> allArrime() {
        return arrimeRepository.findAll();
    }

    public boolean existsByNumeroTicket(String numeroDeTicket) {
        return arrimeRepository.existsByNumeroTicket(numeroDeTicket);
    }

    @Override
    public Arrime findByNumeroTicket(String ticket) {
        return arrimeRepository.findByNumeroTicket(ticket);
    }

    public String generarNumeroTicket(LocalDateTime fechaCobro) {
        int mes = fechaCobro.getMonthValue();
        int anio = fechaCobro.getYear();

        LocalDateTime inicioMes = YearMonth.of(anio, mes).atDay(1).atStartOfDay();
        LocalDateTime finMes = YearMonth.of(anio, mes).atEndOfMonth().atTime(23, 59, 59, 999999999);

        Optional<Arrime> ultimoArrime = arrimeRepository
                .findTopByFechaCobroLocalBetweenOrderByFechaCobroLocalDesc(inicioMes, finMes);

        int correlativo = 1;

        if (ultimoArrime.isPresent() && ultimoArrime.get().getNumeroTicket() != null) {
            String ticketAnterior = ultimoArrime.get().getNumeroTicket();
            try {
                String[] partes = ticketAnterior.split("AE-");
                if (partes.length > 1) {
                    String[] subPartes = partes[1].split("-");
                    correlativo = Integer.parseInt(subPartes[0]) + 1;
                }
            } catch (Exception e) {
                log.warn("Error parseando el ticket anterior: {}", e.getMessage());
            }
        }

        return String.format("N. CVM-GGP-GPM-LOS KILOMETROS AE-%02d-%02d-%d", correlativo, mes, anio);
    }

    public List<Arrime> findByMineroId(String mineroId) {
        return arrimeRepository.findByMineroId(mineroId);
    }

    public List<Arrime> obtenerReporteArrimes(String fechaInicioStr, String fechaFinStr) {
        if (fechaInicioStr == null || fechaFinStr == null) {
            return arrimeRepository.findAll(); // Si no hay fechas, devuelve todo
        }

        // Parseamos las fechas ISO que manda Flutter
        LocalDateTime inicio = LocalDateTime.parse(fechaInicioStr, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime fin = LocalDateTime.parse(fechaFinStr, DateTimeFormatter.ISO_DATE_TIME);

        return arrimeRepository.findByFechaCobroLocalBetweenOrderByFechaCobroLocalDesc(inicio, fin);
    }
}