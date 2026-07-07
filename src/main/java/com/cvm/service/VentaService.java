package com.cvm.service;

import com.cvm.dto.VentaRequest;
import com.cvm.model.Venta;

import java.util.List;
import java.util.Optional;


public interface VentaService{

    Venta procesarVenta(VentaRequest request, String emailCajero);
    Venta findById(String id);
    List<Venta> findAll();
    List<Venta> findByBeneficiarioId(String beneficiarioId);


}
