package com.cvm.repository;


import com.cvm.model.Venta;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface VentaRepository extends MongoRepository<Venta, String> {
}