package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.UUID;

public class CalzadoResponse {

    private UUID id;
    private String nombre;
    private BigDecimal factorComplejidad;

    public CalzadoResponse() {
    }

    public CalzadoResponse(UUID id, String nombre, BigDecimal factorComplejidad) {
        this.id = id;
        this.nombre = nombre;
        this.factorComplejidad = factorComplejidad;
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

    public BigDecimal getFactorComplejidad() {
        return factorComplejidad;
    }

    public void setFactorComplejidad(BigDecimal factorComplejidad) {
        this.factorComplejidad = factorComplejidad;
    }
}
