package com.cvm.model;

public enum EstadoVenta {
    //comentario dde cambio
    PENDIENTE_ENTREGA, // Pagada en el POS, nada despachado físicamente
    ENTREGA_PARCIAL,   // Se despachó una parte, el minero tiene saldo a favor
    ENTREGADA          // El stock físico y la solicitud coinciden
}