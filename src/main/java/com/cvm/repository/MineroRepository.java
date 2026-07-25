package com.cvm.repository;

import com.cvm.model.Minero;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MineroRepository extends MongoRepository<Minero, String> {
    Optional<Minero> findByCedula(String cedula);
    boolean existsByCedula(String cedula);
}