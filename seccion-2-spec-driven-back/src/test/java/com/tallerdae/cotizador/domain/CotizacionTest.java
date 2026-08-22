package com.tallerdae.cotizador.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CotizacionTest {

    @Test
    void crear_conListaVacia_lanzaReparacionesVaciasException() {
        Calzado calzado = new Calzado(UUID.randomUUID(), "Test", new BigDecimal("1.5"));
        UrgencyPricingStrategy strategy = new NonUrgentPricingStrategy();

        assertThrows(ReparacionesVaciasException.class,
                () -> Cotizacion.crear(calzado, new ArrayList<>(), false, strategy));
    }

    @Test
    void crear_conListaNula_lanzaReparacionesVaciasException() {
        Calzado calzado = new Calzado(UUID.randomUUID(), "Test", new BigDecimal("1.5"));
        UrgencyPricingStrategy strategy = new NonUrgentPricingStrategy();

        assertThrows(ReparacionesVaciasException.class,
                () -> Cotizacion.crear(calzado, null, false, strategy));
    }

    @Test
    void crear_conEntradaValida_generaIdYFechaNoNulos() {
        Calzado calzado = new Calzado(UUID.randomUUID(), "Test", new BigDecimal("1.5"));
        Reparacion reparacion = new Reparacion(UUID.randomUUID(), "Test", new BigDecimal("20.00"), 3);
        UrgencyPricingStrategy strategy = new NonUrgentPricingStrategy();

        Cotizacion cotizacion = Cotizacion.crear(calzado, List.of(reparacion), false, strategy);

        assertNotNull(cotizacion.getId());
        assertNotNull(cotizacion.getFechaCreacion());
    }
}
