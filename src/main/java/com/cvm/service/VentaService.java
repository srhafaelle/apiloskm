package com.cvm.service;

import com.cvm.dto.VentaRequest;
import com.cvm.model.Venta;

import java.util.Optional;


public interface VentaService{

    Venta procesarVenta(VentaRequest request, String emailCajero);
    Venta findById(String id);


}
