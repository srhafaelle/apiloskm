package com.cvm.service;

import com.cvm.dto.DespachoPOSRequest;
import com.cvm.model.Despacho;

public interface DespachoService {

    Despacho registrarDespachoGlobal(DespachoPOSRequest request, String emailCajero);
    //comentario dde cambio

}
