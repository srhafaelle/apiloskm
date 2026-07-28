package com.cvm.model;

public enum TipoMinero {
    JEFE_BRIGADA,     // El responsable, el que asume la deuda de arrime
    TRABAJADOR,       // Operario, fuerza laboral (no acumula deuda propia si está en brigada)
    INDEPENDIENTE     // Minero que trabaja solo y asume su propio arrime
}