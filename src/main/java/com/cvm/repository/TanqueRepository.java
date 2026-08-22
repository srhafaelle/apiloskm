package com.cvm.repository;
import com.cvm.model.Tanque;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TanqueRepository extends MongoRepository<Tanque, String> {
}