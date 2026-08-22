package com.cvm.service;
import com.cvm.model.MedicionTanque;
import java.util.List;

public interface IMedicionTanqueService {
    MedicionTanque guardar(MedicionTanque medicion);
    List<MedicionTanque> obtenerHistorial(String fechaInicio, String fechaFin);
}