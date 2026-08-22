package com.tallerdae.cotizador.domain;

import java.util.UUID;

public class CotizacionNotFoundException extends RuntimeException {

    private static final String MENSAJE = "Cotización no encontrada: ";

    public CotizacionNotFoundException(UUID id) {
        super(MENSAJE + id);
    }
}
