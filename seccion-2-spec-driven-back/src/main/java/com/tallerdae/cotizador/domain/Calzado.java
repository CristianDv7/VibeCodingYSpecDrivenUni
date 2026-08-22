package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Calzado {

    private final UUID id;
    private final String nombre;
    private final BigDecimal factorComplejidad;

    public Calzado(UUID id, String nombre, BigDecimal factorComplejidad) {
        this.id = id;
        this.nombre = nombre;
        this.factorComplejidad = factorComplejidad;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getFactorComplejidad() {
        return factorComplejidad;
    }
}
