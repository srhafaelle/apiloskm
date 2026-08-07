package com.cvm.repository;

import com.cvm.model.Arrime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArrimeRepository extends MongoRepository<Arrime, String> {


    // Método para buscar el último arrime registrado dentro de un rango de fechas (el mes actual)
    Optional<Arrime> findTopByFechaCobroLocalBetweenOrderByFechaCobroLocalDesc(LocalDateTime inicio, LocalDateTime fin);
    boolean existsByNumeroSeguimiento(String numero);
    boolean existsByNumeroTicket(String numeroDeTicket);
    Arrime findByNumeroTicket(String ticket);
    List<Arrime> findByMineroId(String mineroId);
    List<Arrime> findByFechaCobroLocalBetweenOrderByFechaCobroLocalDesc(LocalDateTime inicio, LocalDateTime fin);

}
