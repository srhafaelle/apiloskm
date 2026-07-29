package com.cvm.repository;
import com.cvm.model.EstadoTurno;
import com.cvm.model.Turno;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TurnoRepository extends MongoRepository<Turno, String> {
    // Busca si un usuario específico tiene un turno en estado ABIERTO
    Optional<Turno> findByUsuarioCajeroIdAndEstado(String usuarioCajeroId, EstadoTurno estado);
    //comentario dde cambio
}