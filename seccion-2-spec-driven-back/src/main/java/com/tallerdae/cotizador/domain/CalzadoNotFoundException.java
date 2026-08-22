package com.tallerdae.cotizador.domain;

import java.util.UUID;

public class CalzadoNotFoundException extends RuntimeException {

    private static final String MENSAJE = "Tipo de calzado no encontrado: ";

    public CalzadoNotFoundException(UUID id) {
        super(MENSAJE + id);
    }
}
