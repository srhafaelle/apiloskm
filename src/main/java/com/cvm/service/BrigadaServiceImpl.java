package com.cvm.service;

import com.cvm.dto.*;
import com.cvm.model.BrigadaMinera;
import com.cvm.model.Despacho;
import com.cvm.model.Minero;
import com.cvm.model.Producto;
import com.cvm.repository.BrigadaMineraRepository;
import com.cvm.repository.MineroRepository;
import com.cvm.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.cvm.model.PlanArrime;
import com.cvm.model.CuotaArrime;
import com.cvm.model.EstadoCuota;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import com.cvm.model.CuotaArrime;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrigadaServiceImpl implements BrigadaService {

    private final BrigadaMineraRepository brigadaRepository;
    private final MineroRepository mineroRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional // Asegura que si falla la actualización del minero, no se guarde la brigada
    public BrigadaMinera createBrigada(BrigadaRequest request) {

        Optional<BrigadaMinera> existente = brigadaRepository.findByNombreBrigada(request.getNombreBrigada());
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una brigada con el nombre: " + request.getNombreBrigada());
        }

        Minero responsable = mineroRepository.findById(request.getMineroResponsableId())
                .orElseThrow(() -> new RuntimeException("Minero responsable no encontrado"));

        BrigadaMinera brigada = BrigadaMinera.builder()
                .nombreBrigada(request.getNombreBrigada())
                .build();

        // Agregar el ID del responsable al Set de mineros y generar el código QR
        brigada.getMinerosIds().add(responsable.getId());
        brigada.generarNumeroUnico();

        BrigadaMinera brigadaGuardada = brigadaRepository.save(brigada);

        // Actualizar el documento del minero para reflejar su nueva brigada
        responsable.setBrigadaActualId(brigadaGuardada.getId());
        mineroRepository.save(responsable);

        return brigadaGuardada;
    }

    @Override
    @Transactional
    public BrigadaMinera addMineroToBrigada(String brigadaId, String mineroId) {
        BrigadaMinera brigada = brigadaRepository.findById(brigadaId)
                .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

        Minero minero = mineroRepository.findById(mineroId)
                .orElseThrow(() -> new RuntimeException("Minero no encontrado"));

        brigada.getMinerosIds().add(minero.getId());
        minero.setBrigadaActualId(brigada.getId());

        mineroRepository.save(minero);
        return brigadaRepository.save(brigada);
    }

    @Override
    public List<BrigadaMinera> getAllBrigadas() {
        return brigadaRepository.findAll();
    }

    @Override
    public BrigadaMinera registrarPagoInscripcion(String brigadaId, Double montoOro) {
        BrigadaMinera brigada = brigadaRepository.findById(brigadaId)
                .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

        // Sumamos el abono
        Double nuevoSaldo = brigada.getOroPagadoHastaLaFecha() + montoOro;
        brigada.setOroPagadoHastaLaFecha(nuevoSaldo);

        return brigadaRepository.save(brigada);
    }

    @Override
    @Transactional
    public BrigadaMinera registrarDespacho(String brigadaId, DespachoRequest request, String usuarioAdminEmail) {
        BrigadaMinera brigada = brigadaRepository.findById(brigadaId)
                .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el inventario"));

        if (!producto.isActivo()) {
            throw new RuntimeException("El producto seleccionado no está activo para despachos");
        }

        if (producto.getStockDisponible() < request.getCantidad()) {
            throw new RuntimeException("Stock insuficiente. Solo quedan " + producto.getStockDisponible() + " " + producto.getUnidad());
        }
        // Calculamos el costo total en oro (Precio unitario * Cantidad solicitada)
        Double costoTotalDespacho = producto.getPrecioOro() * request.getCantidad();

        producto.setStockDisponible(producto.getStockDisponible() - request.getCantidad());
        producto.setCantidadTotalDespachada(producto.getCantidadTotalDespachada() + request.getCantidad());
        producto.setOroRecaudadoHistorico(producto.getOroRecaudadoHistorico() + costoTotalDespacho);
        productoRepository.save(producto); // Guardamos el cambio en el inventario

        // Construimos el registro del despacho
        Despacho nuevoDespacho = Despacho.builder()
                .productoId(producto.getId())
                .nombreProducto(producto.getNombre())
                .cantidadEntregada(request.getCantidad())
                .costoEnOro(costoTotalDespacho)
                .fechaDespacho(LocalDateTime.now())
                .despachadoPorUsuarioId(usuarioAdminEmail) // Registramos quién autorizó
                .build();

        // Agregamos el historial a la brigada
        brigada.getHistorialDespachos().add(nuevoDespacho);

        return brigadaRepository.save(brigada);
    }
    @Override
    public BrigadaMinera asignarPlanArrime(String brigadaId, Double cuotaMensual) {
        BrigadaMinera brigada = brigadaRepository.findById(brigadaId)
                .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

        // Regla de negocio: No pueden tener plan si no han pagado la inscripción
        if (!brigada.inscripcionSolvente()) {
            throw new RuntimeException("La brigada no puede iniciar un plan de arrime sin haber completado los 20g de inscripción.");
        }

        PlanArrime plan = PlanArrime.builder()
                .activo(true)
                .cuotaMensualAsignada(cuotaMensual)
                .fechaInicioPlan(LocalDate.now())
                .build();

        brigada.setPlanArrime(plan);
        return brigadaRepository.save(brigada);
    }

    @Override
    public BrigadaMinera registrarPagoArrime(String brigadaId, Double montoOro) {
        BrigadaMinera brigada = brigadaRepository.findById(brigadaId)
                .orElseThrow(() -> new RuntimeException("Brigada no encontrada"));

        List<CuotaArrime> deudas = brigada.obtenerMesesEnDeuda();
        if (deudas.isEmpty()) {
            throw new RuntimeException("La brigada no tiene cuotas de arrime pendientes por pagar.");
        }

        Double oroRestante = montoOro;

        // Distribución en cascada (Paga las más viejas primero)
        for (CuotaArrime cuota : deudas) {
            if (oroRestante <= 0) break; // Si ya se acabó el oro, salimos del ciclo

            Double saldoPendiente = cuota.getSaldoPendiente();

            if (oroRestante >= saldoPendiente) {
                // El oro alcanza para liquidar esta cuota completa
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + saldoPendiente);
                cuota.setEstado(EstadoCuota.PAGADA);
                cuota.setFechaPagoCompletado(LocalDateTime.now());
                oroRestante -= saldoPendiente;
            } else {
                // El oro solo alcanza para un abono parcial
                cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + oroRestante);
                oroRestante = 0.0;
            }
        }

        return brigadaRepository.save(brigada);
    }

    // Tarea programada: Se ejecuta automáticamente el día 1 de cada mes a la 00:00
    @Override
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generarCuotasMensuales() {
        List<BrigadaMinera> brigadas = brigadaRepository.findAll();
        LocalDate hoy = LocalDate.now();

        // Obtiene el nombre del mes en español (ej: "junio 2026")
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        String periodoActual = mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + hoy.getYear();

        for (BrigadaMinera brigada : brigadas) {
            if (brigada.getPlanArrime() != null && brigada.getPlanArrime().isActivo()) {

                // 1. Marcar como VENCIDAS las cuotas pendientes del mes pasado
                brigada.getHistorialCuotas().stream()
                        .filter(c -> c.getEstado() == EstadoCuota.PENDIENTE && c.getFechaVencimiento().isBefore(hoy))
                        .forEach(c -> c.setEstado(EstadoCuota.VENCIDA));

                // 2. Generar la nueva cuota para el mes que empieza
                CuotaArrime nuevaCuota = CuotaArrime.builder()
                        .periodo(periodoActual)
                        .montoExigidoOro(brigada.getPlanArrime().getCuotaMensualAsignada())
                        .fechaVencimiento(hoy.plusMonths(1).withDayOfMonth(1).minusDays(1)) // Último día del mes actual
                        .build();

                brigada.getHistorialCuotas().add(nuevaCuota);
                brigadaRepository.save(brigada);
            }
        }
    }
    @Override
    public DashboardResponse obtenerMetricasDashboard() {
        List<BrigadaMinera> todasLasBrigadas = brigadaRepository.findAll();

        // 1. Cálculos de Inscripciones
        Double totalInscripciones = todasLasBrigadas.stream()
                .mapToDouble(BrigadaMinera::getOroPagadoHastaLaFecha)
                .sum();

        // 2. Cálculos de Arrime (Recaudado y Deuda)
        Double totalArrimeRecaudado = 0.0;
        Double totalDeudaArrime = 0.0;

        for (BrigadaMinera b : todasLasBrigadas) {
            // Sumamos lo que esta brigada ha pagado en todas sus cuotas
            if (b.getHistorialCuotas() != null) {
                totalArrimeRecaudado += b.getHistorialCuotas().stream()
                        .mapToDouble(CuotaArrime::getMontoPagadoOro)
                        .sum();
            }
            // Sumamos lo que esta brigada debe en total
            totalDeudaArrime += b.obtenerDeudaTotalArrime();
        }

        // 3. Identificar a los morosos (los que tienen deuda > 0)
        List<BrigadaMorosaDTO> morosos = todasLasBrigadas.stream()
                .filter(b -> b.obtenerDeudaTotalArrime() > 0)
                .map(b -> BrigadaMorosaDTO.builder()
                        .brigadaId(b.getId())
                        .nombreBrigada(b.getNombreBrigada())
                        .numeroUnicoRegistro(b.getNumeroUnicoRegistro())
                        .deudaTotalOro(b.obtenerDeudaTotalArrime())
                        .mesesAtraso(b.obtenerMesesEnDeuda().size())
                        .build())
                // Ordenamos para que los que deben más oro salgan de primeros
                .sorted(Comparator.comparing(BrigadaMorosaDTO::getDeudaTotalOro).reversed())
                .collect(Collectors.toList());

        // 4. Construir la respuesta final
        return DashboardResponse.builder()
                .totalBrigadasRegistradas(todasLasBrigadas.size())
                .totalBrigadasMorosas(morosos.size())
                .totalOroRecaudadoInscripciones(totalInscripciones)
                .totalOroRecaudadoArrime(totalArrimeRecaudado)
                .totalOroDeudaArrime(totalDeudaArrime)
                .topMorosos(morosos)
                .build();
    }

    // Asegúrate de importar java.time.LocalDate;

    @Override
    public TesoreriaResponse obtenerTesoreriaPorFechas(LocalDate inicio, LocalDate fin) {
        List<BrigadaMinera> todasLasBrigadas = brigadaRepository.findAll();

        final Double[] totalVentas = {0.0};
        final Double[] totalInscripciones = {0.0};
        final Double[] totalArrime = {0.0};

        for (BrigadaMinera brigada : todasLasBrigadas) {

            // 1. Calcular Inscripciones (Tomamos la fecha en la que se registró la brigada)
            if (brigada.getFechaRegistro() != null) {
                LocalDate fechaReg = brigada.getFechaRegistro().toLocalDate(); // Asumiendo que es LocalDateTime
                if (!fechaReg.isBefore(inicio) && !fechaReg.isAfter(fin)) {
                    totalInscripciones[0] += (brigada.getOroPagadoHastaLaFecha() != null) ? brigada.getOroPagadoHastaLaFecha() : 0.0;
                }
            }

            // 2. Calcular Ventas (Despachos)
            if (brigada.getHistorialDespachos() != null) {
                brigada.getHistorialDespachos().forEach(despacho -> {
                    LocalDate fechaDespacho = despacho.getFechaDespacho().toLocalDate();
                    if (!fechaDespacho.isBefore(inicio) && !fechaDespacho.isAfter(fin)) {
                        totalVentas[0] += despacho.getCostoEnOro();
                    }
                });
            }

            // 3. Calcular Cuotas de Arrime (Solo las pagadas)
            if (brigada.getHistorialCuotas() != null) {
                brigada.getHistorialCuotas().forEach(cuota -> {
                    // Validamos que tenga fecha de pago (significa que ya pagó)
                    if (cuota.getFechaPago() != null) {
                        LocalDate fechaPago = cuota.getFechaPago().toLocalDate();
                        if (!fechaPago.isBefore(inicio) && !fechaPago.isAfter(fin)) {
                            totalArrime[0] += cuota.getMontoPagadoOro();
                        }
                    }
                });
            }
        }

        return new TesoreriaResponse(totalVentas[0], totalInscripciones[0], totalArrime[0]);
    }
}