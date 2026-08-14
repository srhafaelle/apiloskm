package com.cvm.repository;
import com.cvm.model.CargaInsumo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CargaInsumoRepository extends MongoRepository<CargaInsumo, String> {

//comentario dde cambio
List<CargaInsumo> findByProductoIdOrderByFechaDeRecepcionDesc(String productoId);
}