package com.cvm.repository;

import com.cvm.model.Mina;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MinaRepository extends MongoRepository<Mina, String> {
    List<Mina> findBySectorMineroId(String sectorMineroId);
}