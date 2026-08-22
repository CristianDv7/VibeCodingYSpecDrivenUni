package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public class ReparacionResponse {

    private UUID id;
    private String nombre;
    private BigDecimal precioBase;
    private int tiempoEstimadoDias;

    public ReparacionResponse() {
    }

    public ReparacionResponse(UUID id, String nombre, BigDecimal precioBase, int tiempoEstimadoDias) {
        this.id = id;
        this.nombre = nombre;
        this.precioBase = precioBase;
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public int getTiempoEstimadoDias() {
        return tiempoEstimadoDias;
    }

    public void setTiempoEstimadoDias(int tiempoEstimadoDias) {
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }
}
