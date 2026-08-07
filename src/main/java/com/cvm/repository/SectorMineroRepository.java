package com.cvm.repository;

import com.cvm.model.SectorMinero;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorMineroRepository extends MongoRepository<SectorMinero, String> {
}