package com.cvm.repository;

import com.cvm.model.PuntoDistribucion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PuntoDistribucionRepository extends MongoRepository<PuntoDistribucion, String> {
    //comentario dde cambio
}