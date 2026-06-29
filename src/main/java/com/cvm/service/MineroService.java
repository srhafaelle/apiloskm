package com.cvm.service;

import com.cvm.dto.MineroRequest;
import com.cvm.model.Minero;
import java.util.List;

public interface MineroService {
    Minero createMinero(MineroRequest request);
    List<Minero> getAllMineros();
    Minero getMineroById(String id);
}