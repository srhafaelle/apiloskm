package com.cvm.service;

import com.cvm.dto.ArrimeSyncRequest;
import com.cvm.model.Arrime;
import com.cvm.repository.ArrimeRepository;

import java.util.List;

public interface ArrimeService  {


    public void procesarArrime(ArrimeSyncRequest request, String contralorEmail);

    public void procesarArrimesEnLote(List<ArrimeSyncRequest> requests, String contralorEmail);

    public List<Arrime> allArrime();

    public boolean existsByNumeroTicket(String numeroDeTicket);

    public Arrime findByNumeroTicket(String ticket);
}
