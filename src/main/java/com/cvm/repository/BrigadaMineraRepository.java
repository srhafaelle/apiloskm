package com.cvm.repository;

import com.cvm.model.BrigadaMinera;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface BrigadaMineraRepository extends MongoRepository<BrigadaMinera, String> {
    Optional<BrigadaMinera> findByNumeroUnicoRegistro(String numeroUnicoRegistro);
    Optional<BrigadaMinera> findByNombreBrigada(String nombreBrigada);
}