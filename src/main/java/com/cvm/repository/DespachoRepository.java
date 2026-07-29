package com.cvm.repository;

import com.cvm.model.Despacho;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DespachoRepository extends MongoRepository<Despacho, String> {
    //comentario dde cambio
}
