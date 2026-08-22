package com.tallerdae.cotizador.domain;

public class ReparacionesVaciasException extends RuntimeException {

    private static final String MENSAJE = "Se requiere al menos una reparación";

    public ReparacionesVaciasException() {
        super(MENSAJE);
    }
}
