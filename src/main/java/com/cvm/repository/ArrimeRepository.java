package com.cvm.repository;

import com.cvm.model.Arrime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArrimeRepository extends MongoRepository<Arrime, String> {

    boolean existsByNumeroTicket(String numeroDeTicket);
    //comentario dde cambio
}
