package com.cvm.service;

import com.cvm.dto.MineroDTO;
import com.cvm.dto.MineroRequest;
import com.cvm.model.*;
import com.cvm.repository.ArrimeRepository;
import com.cvm.repository.MineroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MineroServiceImpl implements MineroService {

    private final MineroRepository mineroRepository;
    private final ArrimeRepository arrimeRepository;

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
                .tipoMinero(request.getTipoMinero() != null ? request.getTipoMinero() : TipoMinero.TRABAJADOR)
                .cuotaInscripcionOro(request.getCuotaInscripcionOro() != null ? request.getCuotaInscripcionOro() : 20.0)
                .sectorMineroIds(request.getSectorMineroIds()) // Lista de Strings
                .equipos(request.getEquipos()) // Lista de objetos Equipo
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
        minero.setSectorMineroIds(request.getSectorMineroIds());
        minero.setEquipos(request.getEquipos());
        minero.setCargo(request.getCargo());

        if (request.getTipoMinero() != null && request.getTipoMinero() != minero.getTipoMinero()) {
            if (minero.getBrigadaActualId() != null && minero.getTipoMinero() == TipoMinero.JEFE_BRIGADA) {
                throw new RuntimeException("No puede degradar a un Jefe de Brigada mientras esté asignado a una. Retírelo primero.");
            }
            minero.setTipoMinero(request.getTipoMinero());
        }

        return mineroRepository.save(minero);
    }





    // ==========================================
    // GENERACIÓN DEL DTO (La Magia de los cálculos al vuelo)
    // ==========================================
    @Override
    public MineroDTO obtenerPerfilMinero(String mineroId) {
        Minero minero = getMineroById(mineroId);

        // Buscamos el historial real y único: Los Arrimes
        List<Arrime> arrimes = arrimeRepository.findByMineroId(mineroId);

        // 1. Calculamos cómo va con la Inscripción
        double oroPagadoInscripcion = arrimes.stream()
                .filter(a -> a.getTipoDeArrime() == TipoDeArrime.INSCRIPCION)
                .mapToDouble(Arrime::getMontoOro)
                .sum();

        double deudaInscripcion = Math.max(0, minero.getCuotaInscripcionOro() - oroPagadoInscripcion);
        boolean solvente = oroPagadoInscripcion >= minero.getCuotaInscripcionOro();

        // 2. Calculamos el Total de Oro Arrimado (Todo lo que no sea inscripción)
        double totalOroArrimado = arrimes.stream()
                .filter(a -> a.getTipoDeArrime() != TipoDeArrime.INSCRIPCION)
                .mapToDouble(Arrime::getMontoOro)
                .sum();

        return MineroDTO.builder()
                .id(minero.getId())
                .nombres(minero.getNombres())
                .apellidos(minero.getApellidos())
                .cedula(minero.getCedula())
                .cargo(minero.getCargo())
                .tipoMinero(minero.getTipoMinero())
                .numeroUnicoRegistro(minero.getNumeroUnicoRegistro())
                .operacionesParalizadas(minero.isOperacionesParalizadas())
                .cuotaInscripcionOro(minero.getCuotaInscripcionOro())
                .sectorMineroIds(minero.getSectorMineroIds())
                .equipos(minero.getEquipos())
                .planArrime(minero.getPlanArrime())
                // Campos Calculados Dinámicamente:
                .oroPagadoHastaLaFecha(oroPagadoInscripcion) // Solo para saber la inscripción
                .deudaInscripcionRestante(deudaInscripcion)
                .inscripcionSolvente(solvente)

                .build();
    }

    @Override
    public List<MineroDTO> obtenerTodosLosPerfiles() {
        // Buscamos todos los mineros crudos
        List<Minero> mineros = mineroRepository.findAll();

        // Los convertimos uno por uno a MineroDTO usando la lógica que ya creamos
        return mineros.stream()
                .map(minero -> obtenerPerfilMinero(minero.getId()))
                .toList();
    }

}