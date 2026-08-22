package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class UrgentPricingStrategy implements UrgencyPricingStrategy {

    @Override
    public BigDecimal calcularRecargo(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal recargo) {
        return subtotal.add(recargo);
    }

    @Override
    public int calcularTiempoEntrega(List<Reparacion> reparaciones) {
        int maxDias = reparaciones.stream()
                .mapToInt(Reparacion::getTiempoEstimadoDias)
                .max()
                .orElse(0);
        int techo = (int) Math.ceil(maxDias / 2.0);
        return Math.max(techo, 1);
    }
}
