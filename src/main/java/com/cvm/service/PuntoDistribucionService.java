package com.cvm.service;

import com.cvm.dto.PuntoDistribucionRequest;
import com.cvm.model.PuntoDistribucion;
import java.util.List;

public interface PuntoDistribucionService {
    PuntoDistribucion createPuntoDistribucion(PuntoDistribucionRequest request);
    List<PuntoDistribucion> getAllPuntos();
    PuntoDistribucion toggleActivo(String id); // Para pausar un centro sin borrarlo
}