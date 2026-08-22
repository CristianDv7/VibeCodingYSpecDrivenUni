package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class Reparacion {

    private final UUID id;
    private final String nombre;
    private final BigDecimal precioBase;
    private final int tiempoEstimadoDias;

    public Reparacion(UUID id, String nombre, BigDecimal precioBase, int tiempoEstimadoDias) {
        this.id = id;
        this.nombre = nombre;
        this.precioBase = precioBase;
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public int getTiempoEstimadoDias() {
        return tiempoEstimadoDias;
    }
}
