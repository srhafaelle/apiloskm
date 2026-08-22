package com.cvm.repository;
import com.cvm.model.TransaccionOro;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionOroRepository extends MongoRepository<TransaccionOro, String> {
    List<TransaccionOro> findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(LocalDateTime inicio, LocalDateTime fin);
}