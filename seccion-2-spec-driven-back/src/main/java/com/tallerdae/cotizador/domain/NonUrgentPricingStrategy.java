package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.util.List;

public class NonUrgentPricingStrategy implements UrgencyPricingStrategy {

    @Override
    public BigDecimal calcularRecargo(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal recargo) {
        return subtotal;
    }

    @Override
    public int calcularTiempoEntrega(List<Reparacion> reparaciones) {
        return reparaciones.stream()
                .mapToInt(Reparacion::getTiempoEstimadoDias)
                .max()
                .orElse(0);
    }
}
