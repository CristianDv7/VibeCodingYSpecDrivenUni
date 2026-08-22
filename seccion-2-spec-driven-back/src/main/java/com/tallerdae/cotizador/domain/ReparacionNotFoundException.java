package com.tallerdae.cotizador.domain;

import java.util.UUID;

public class ReparacionNotFoundException extends RuntimeException {

    private static final String MENSAJE = "Reparación no encontrada: ";

    public ReparacionNotFoundException(UUID id) {
        super(MENSAJE + id);
    }
}
