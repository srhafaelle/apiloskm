package com.cvm.repository;
import com.cvm.model.Draguero;
import org.springframework.data.mongodb.repository.MongoRepository; // O JpaRepository
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DragueroRepository extends MongoRepository<Draguero, String> {
    Optional<Draguero> findByCedula(String cedula);

    // Método para el buscador del frontend
    List<Draguero> findByNombresContainingIgnoreCaseOrCedulaContainingIgnoreCase(String nombres, String cedula);
}