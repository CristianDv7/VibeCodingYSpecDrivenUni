package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CotizacionResponse {

    private UUID id;
    private LocalDateTime fechaCreacion;
    private BigDecimal subtotal;
    private BigDecimal recargoUrgencia;
    private BigDecimal total;
    private int tiempoEstimadoDias;

    public CotizacionResponse() {
    }

    public CotizacionResponse(UUID id, LocalDateTime fechaCreacion, BigDecimal subtotal,
                              BigDecimal recargoUrgencia, BigDecimal total, int tiempoEstimadoDias) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.subtotal = subtotal;
        this.recargoUrgencia = recargoUrgencia;
        this.total = total;
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getRecargoUrgencia() {
        return recargoUrgencia;
    }

    public void setRecargoUrgencia(BigDecimal recargoUrgencia) {
        this.recargoUrgencia = recargoUrgencia;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getTiempoEstimadoDias() {
        return tiempoEstimadoDias;
    }

    public void setTiempoEstimadoDias(int tiempoEstimadoDias) {
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }
}
