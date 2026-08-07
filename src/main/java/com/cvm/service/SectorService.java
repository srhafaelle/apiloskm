package com.cvm.service;

import com.cvm.model.SectorMinero;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SectorService  {

    SectorService findBySectorId(String sectorId);
}
