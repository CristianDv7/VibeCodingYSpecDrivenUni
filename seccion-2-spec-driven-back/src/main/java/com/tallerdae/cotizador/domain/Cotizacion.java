package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Cotizacion {

    private final UUID id;
    private final LocalDateTime fechaCreacion;
    private final Calzado calzado;
    private final List<Reparacion> reparaciones;
    private final boolean esUrgente;
    private final BigDecimal subtotal;
    private final BigDecimal recargoUrgencia;
    private final BigDecimal total;
    private final int tiempoEstimadoDias;

    private Cotizacion(UUID id, LocalDateTime fechaCreacion, Calzado calzado,
                       List<Reparacion> reparaciones, boolean esUrgente,
                       BigDecimal subtotal, BigDecimal recargoUrgencia,
                       BigDecimal total, int tiempoEstimadoDias) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.calzado = calzado;
        this.reparaciones = reparaciones;
        this.esUrgente = esUrgente;
        this.subtotal = subtotal;
        this.recargoUrgencia = recargoUrgencia;
        this.total = total;
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }

    public static Cotizacion crear(Calzado calzado, List<Reparacion> reparaciones,
                                   boolean esUrgente, UrgencyPricingStrategy strategy) {
        if (reparaciones == null || reparaciones.isEmpty()) {
            throw new ReparacionesVaciasException();
        }

        UUID id = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now();

        BigDecimal subtotal = reparaciones.stream()
                .map(r -> r.getPrecioBase().multiply(calzado.getFactorComplejidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal recargoUrgencia = strategy.calcularRecargo(subtotal);
        BigDecimal total = strategy.calcularTotal(subtotal, recargoUrgencia);
        int tiempoEstimadoDias = strategy.calcularTiempoEntrega(reparaciones);

        return new Cotizacion(id, fechaCreacion, calzado, reparaciones, esUrgente,
                subtotal, recargoUrgencia, total, tiempoEstimadoDias);
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Calzado getCalzado() {
        return calzado;
    }

    public List<Reparacion> getReparaciones() {
        return reparaciones;
    }

    public boolean isEsUrgente() {
        return esUrgente;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getRecargoUrgencia() {
        return recargoUrgencia;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public int getTiempoEstimadoDias() {
        return tiempoEstimadoDias;
    }
}
