package com.tallerdae.cotizador.domain;

import java.math.BigDecimal;
import java.util.List;

public interface UrgencyPricingStrategy {

    BigDecimal calcularRecargo(BigDecimal subtotal);

    BigDecimal calcularTotal(BigDecimal subtotal, BigDecimal recargo);

    int calcularTiempoEntrega(List<Reparacion> reparaciones);
}
