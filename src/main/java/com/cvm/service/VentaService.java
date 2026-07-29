package com.cvm.service;

import com.cvm.dto.VentaRequest;
import com.cvm.model.Venta;

import java.util.List;
import java.util.Optional;


public interface VentaService{
    //comentario dde cambio
    Venta procesarVenta(VentaRequest request, String emailCajero);
    Venta findById(String id);
    List<Venta> findAll();
    List<Venta> findByBeneficiarioId(String beneficiarioId);
    List<Venta> obtenerCreditosPendientes();
    Venta pagarCredito(String ventaId, String emailCajero);
    List<Venta> obtenerVentasPendientesDeDespacho();
    Venta procesarDespachoFisico(String ventaId, Double cantidadDespachada);


}
