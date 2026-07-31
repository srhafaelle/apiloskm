package com.cvm.service;
import com.cvm.dto.ArrimeSyncRequest;
import com.cvm.model.Arrime;
import com.cvm.model.CuotaArrime;
import com.cvm.model.EstadoCuota;
import com.cvm.model.Minero;
import com.cvm.repository.ArrimeRepository;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArrimeService {

    private final MineroRepository mineroRepository;
    private final ArrimeRepository arrimeRepository; // Necesitas crear este repositorio Mongo si no lo tienes

    @Transactional
    public void procesarArrime(ArrimeSyncRequest request, String contralorEmail) {
        log.info("Procesando arrime/ticket {} para el minero {}", request.getNumeroTicket(), request.getMineroId());

        // 1. Buscar al Minero
        Minero minero = mineroRepository.findById(request.getMineroId())
                .orElseThrow(() -> new RuntimeException("Minero no encontrado con ID: " + request.getMineroId()));

        double saldoAbono = request.getMontoOro();

        // 2. Si el cobro es ESPONTANEO o ABONO_CUOTA, intentamos pagar la deuda mensual
        if ("ABONO_CUOTA".equals(request.getTipoCobro()) || "ESPONTANEO".equals(request.getTipoCobro())) {

            // Recorremos las cuotas buscando deudas (ordenadas por defecto)
            for (CuotaArrime cuota : minero.getHistorialCuotas()) {
                if (saldoAbono <= 0) break; // Si ya consumimos todo el abono, terminamos el ciclo

                if (cuota.getEstado() != EstadoCuota.PAGADA) {
                    double deudaCuota = cuota.getSaldoPendiente();

                    // Almacenamos los datos de auditoría del pago en la cuota
                    cuota.setNumeroTicket(request.getNumeroTicket());
                    cuota.setContralorEmailId(contralorEmail);
                    cuota.setFechaCobroLocal(request.getFechaCobroLocal());
                    cuota.setTipoCobro(request.getTipoCobro());

                    if (saldoAbono >= deudaCuota) {
                        // El abono cubre o supera la deuda de esta cuota
                        cuota.setMontoPagadoOro(cuota.getMontoExigidoOro());
                        cuota.setEstado(EstadoCuota.PAGADA);
                        cuota.setFechaPagoCompletado(LocalDateTime.now());
                        saldoAbono -= deudaCuota; // Sobrante para la siguiente cuota (si hay)
                    } else {
                        // El abono solo cubre una parte de la cuota
                        cuota.setMontoPagadoOro(cuota.getMontoPagadoOro() + saldoAbono);
                        cuota.setEstado(EstadoCuota.PARCIAL);
                        saldoAbono = 0;
                    }
                }
            }
        }

        // 3. Si el cobro es PORCENTAJE_DIARIO o sobró saldo (no había cuotas pendientes)
        // Debemos generar un nuevo registro de CuotaArrime como "ingreso extra" o "pago directo"
        // para que quede la trazabilidad del ticket en el documento del Minero, ya que
        // la entidad CuotaArrime es la que almacena los tickets ahora.
        if (saldoAbono > 0 || "PORCENTAJE_DIARIO".equals(request.getTipoCobro())) {

            CuotaArrime ingresoExtra = CuotaArrime.builder()
                    .idCuota(UUID.randomUUID().toString())
                    .periodo(request.getTipoCobro() + " (Arrime)") // Ej: "PORCENTAJE_DIARIO (Directo)"
                    .numeroTicket(request.getNumeroTicket())
                    .contralorEmailId(contralorEmail)
                    .fechaCobroLocal(request.getFechaCobroLocal())
                    .montoExigidoOro(request.getMontoOro())
                    .montoPagadoOro(request.getMontoOro())
                    .tipoCobro(request.getTipoCobro())
                    .estado(EstadoCuota.PAGADA)
                    .fechaPagoCompletado(LocalDateTime.now())
                    .build();

            // Lo agregamos al inicio de la lista
            minero.getHistorialCuotas().add(0, ingresoExtra);
        }

        // 4. Guardar cambios en el Minero
        mineroRepository.save(minero);
    }
    // Método para sincronización en lote (Bulk) desde Hive
    @Transactional
    public void procesarArrimesEnLote(List<ArrimeSyncRequest> requests, String contralorEmail) {
        log.info("Iniciando sincronización de {} arrimes offline", requests.size());
        for (ArrimeSyncRequest req : requests) {
            try {
                // Verificar si el ticket ya existe para evitar duplicados por mala red
                if (arrimeRepository.existsByNumeroTicket(req.getNumeroTicket())) {
                    log.warn("El ticket {} ya existe en la BD. Saltando...", req.getNumeroTicket());
                    continue;
                }
                //comentario dde cambio
                procesarArrime(req, contralorEmail);
            } catch (Exception e) {
                log.error("Error al sincronizar el ticket {}: {}", req.getNumeroTicket(), e.getMessage());
                // Podrías lanzar la excepción si quieres que falle toda la transacción,
                // o continuar con los demás. Aquí continuamos para no trancar la cola entera por 1 error.
            }
        }
    }

    @Transactional
    public List<Arrime> allArrime(){
       List<Arrime> arrimes = arrimeRepository.findAll();
       return  arrimes;

    }
}