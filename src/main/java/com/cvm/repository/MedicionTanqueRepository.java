package com.cvm.repository;
import com.cvm.model.MedicionTanque;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicionTanqueRepository extends MongoRepository<MedicionTanque, String> {
    // Spring Data Mongo es lo suficientemente inteligente para buscar entre Strings
    List<MedicionTanque> findByFechaMedicionBetweenOrderByFechaMedicionDesc(String inicio, String fin);
}