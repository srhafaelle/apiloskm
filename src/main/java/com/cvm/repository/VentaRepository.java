package com.cvm.repository;
import com.cvm.model.Venta;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface VentaRepository extends MongoRepository<Venta, String> {


        List<Venta> findByTurnoId(String turnoId);

    List<Venta> findByBeneficiarioIdOrderByFechaVentaDesc(String beneficiarioId);

}