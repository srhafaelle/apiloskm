package com.cvm.repository;

import com.cvm.model.Minero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MineroRepository extends MongoRepository<Minero, String> {
    Optional<Minero> findByCedula(String cedula);
    boolean existsByCedula(String cedula);
    // NUEVO MÉTODO PARA BUSCAR Y PAGINAR
    Page<Minero> findByCedulaContaining(
            String cedula, String nombres, String apellidos, Pageable pageable);
}