package com.tallerdae.cotizador.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrgentPricingStrategyTest {

    private final UrgentPricingStrategy strategy = new UrgentPricingStrategy();

    @Test
    void calcularRecargo_conSubtotal48_retorna14punto40() {
        BigDecimal subtotal = new BigDecimal("48.00");

        BigDecimal recargo = strategy.calcularRecargo(subtotal);

        assertEquals(0, new BigDecimal("14.40").compareTo(recargo),
                "El recargo de 48.00 * 0.30 debe ser 14.40");
    }

    @Test
    void calcularTotal_sumaSubtotalMasRecargo() {
        BigDecimal subtotal = new BigDecimal("48.00");
        BigDecimal recargo = new BigDecimal("14.40");

        BigDecimal total = strategy.calcularTotal(subtotal, recargo);

        assertEquals(0, new BigDecimal("62.40").compareTo(total),
                "El total debe ser subtotal + recargo = 62.40");
    }

    @Test
    void calcularTiempoEntrega_aplicaTechoDeMitadConMinimo1() {
        // Caso 1: tiempos [3, 2, 1] → max=3, ceil(3/2)=2
        List<Reparacion> reparaciones1 = List.of(
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 3),
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 2),
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 1)
        );
        assertEquals(2, strategy.calcularTiempoEntrega(reparaciones1),
                "ceil(3/2) debe ser 2");

        // Caso 2: tiempo [1] → max=1, ceil(1/2)=1 (minimo 1)
        List<Reparacion> reparaciones2 = List.of(
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 1)
        );
        assertEquals(1, strategy.calcularTiempoEntrega(reparaciones2),
                "ceil(1/2) = 1, minimo 1");

        // Caso 3: tiempos [5, 4] → max=5, ceil(5/2)=3
        List<Reparacion> reparaciones3 = List.of(
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 5),
                new Reparacion(UUID.randomUUID(), "Test", BigDecimal.ONE, 4)
        );
        assertEquals(3, strategy.calcularTiempoEntrega(reparaciones3),
                "ceil(5/2) debe ser 3");
    }
}
