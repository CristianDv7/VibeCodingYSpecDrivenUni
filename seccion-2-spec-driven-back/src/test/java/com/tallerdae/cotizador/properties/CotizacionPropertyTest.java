package com.tallerdae.cotizador.properties;

import com.tallerdae.cotizador.domain.Calzado;
import com.tallerdae.cotizador.domain.Cotizacion;
import com.tallerdae.cotizador.domain.NonUrgentPricingStrategy;
import com.tallerdae.cotizador.domain.Reparacion;
import com.tallerdae.cotizador.domain.ReparacionesVaciasException;
import com.tallerdae.cotizador.domain.UrgentPricingStrategy;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Feature: cotizador-reparacion-calzado, Properties 3–9
 *
 * Pruebas de propiedad para la entidad Cotizacion y sus reglas de negocio.
 */
class CotizacionPropertyTest {

    /**
     * Property 3: subtotalEsSumaProductos
     *
     * Para cualquier Calzado con factorComplejidad > 0 y lista no vacía de Reparacion
     * con precioBase > 0, el subtotal de la cotización debe ser igual a
     * Σ(precioBase_i × factorComplejidad) con escala 2 HALF_UP.
     *
     * Validates: Requirements 3.1
     */
    @Property
    void subtotalEsSumaProductos(@ForAll("calzadoAleatorio") Calzado calzado,
                                 @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, false, new NonUrgentPricingStrategy());

        BigDecimal expectedSubtotal = reparaciones.stream()
                .map(r -> r.getPrecioBase().multiply(calzado.getFactorComplejidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(cotizacion.getSubtotal()).isEqualByComparingTo(expectedSubtotal);
    }

    /**
     * Property 4: cotizacionNoUrgenteRecargoEsCero
     *
     * Para cualquier cotización con esUrgente = false,
     * recargoUrgencia == 0 y total == subtotal.
     *
     * Validates: Requirements 3.2
     */
    @Property
    void cotizacionNoUrgenteRecargoEsCero(@ForAll("calzadoAleatorio") Calzado calzado,
                                          @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, false, new NonUrgentPricingStrategy());

        assertThat(cotizacion.getRecargoUrgencia()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(cotizacion.getTotal()).isEqualByComparingTo(cotizacion.getSubtotal());
    }

    /**
     * Property 5: tiempoNoUrgenteEsMaximo
     *
     * Para cualquier lista de reparaciones y cotización con esUrgente = false,
     * tiempoEstimadoDias == max(reparacion.tiempoEstimadoDias).
     *
     * Validates: Requirements 3.3
     */
    @Property
    void tiempoNoUrgenteEsMaximo(@ForAll("calzadoAleatorio") Calzado calzado,
                                 @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, false, new NonUrgentPricingStrategy());

        int expectedTiempo = reparaciones.stream()
                .mapToInt(Reparacion::getTiempoEstimadoDias)
                .max()
                .orElse(0);

        assertThat(cotizacion.getTiempoEstimadoDias()).isEqualTo(expectedTiempo);
    }

    /**
     * Property 6: cadaCotizacionTieneIdUnico
     *
     * Para N cotizaciones generadas con entradas válidas, cada una tiene id no nulo,
     * fechaCreacion no nula y ningún par comparte el mismo id.
     *
     * Validates: Requirements 3.4
     */
    @Property
    void cadaCotizacionTieneIdUnico(@ForAll("calzadoAleatorio") Calzado calzado,
                                    @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        int n = 10;
        Set<UUID> ids = new HashSet<>();

        for (int i = 0; i < n; i++) {
            Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, false, new NonUrgentPricingStrategy());
            assertThat(cotizacion.getId()).isNotNull();
            assertThat(cotizacion.getFechaCreacion()).isNotNull();
            ids.add(cotizacion.getId());
        }

        assertThat(ids).hasSize(n);
    }

    @Provide
    Arbitrary<Calzado> calzadoAleatorio() {
        Arbitrary<BigDecimal> factor = Arbitraries.bigDecimals()
                .between(new BigDecimal("0.10"), new BigDecimal("5.00"))
                .ofScale(2);
        return factor.map(f -> new Calzado(UUID.randomUUID(), "TestCalzado", f));
    }

    @Provide
    Arbitrary<List<Reparacion>> reparacionesAleatorias() {
        Arbitrary<Reparacion> reparacion = Arbitraries.bigDecimals()
                .between(new BigDecimal("1.00"), new BigDecimal("100.00"))
                .ofScale(2)
                .flatMap(precio -> Arbitraries.integers().between(1, 30)
                        .map(dias -> new Reparacion(UUID.randomUUID(), "TestRep", precio, dias)));
        return reparacion.list().ofMinSize(1).ofMaxSize(5);
    }

    /**
     * Property 7a: listaVaciaLanzaExcepcion
     *
     * Para cualquier Calzado válido, crear una cotización con lista vacía
     * lanza ReparacionesVaciasException.
     *
     * Validates: Requirements 3.6
     */
    @Property
    void listaVaciaLanzaExcepcion(@ForAll("calzadoAleatorio") Calzado calzado) {
        assertThatThrownBy(() -> Cotizacion.crear(calzado, new ArrayList<>(), false, new NonUrgentPricingStrategy()))
                .isInstanceOf(ReparacionesVaciasException.class);
    }

    /**
     * Property 7b: listaNulaLanzaExcepcion
     *
     * Para cualquier Calzado válido, crear una cotización con lista nula
     * lanza ReparacionesVaciasException.
     *
     * Validates: Requirements 3.6
     */
    @Property
    void listaNulaLanzaExcepcion(@ForAll("calzadoAleatorio") Calzado calzado) {
        assertThatThrownBy(() -> Cotizacion.crear(calzado, null, false, new NonUrgentPricingStrategy()))
                .isInstanceOf(ReparacionesVaciasException.class);
    }

    /**
     * Property 9: cotizacionUrgenteRecargoEsTreintaPorciento
     *
     * Para cualquier subtotal positivo con esUrgente = true,
     * recargoUrgencia == subtotal × 0.30 y total == subtotal + recargoUrgencia.
     *
     * Validates: Requirements 4.1, 4.2
     */
    @Property
    void cotizacionUrgenteRecargoEsTreintaPorciento(@ForAll("calzadoAleatorio") Calzado calzado,
                                                     @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, true, new UrgentPricingStrategy());

        BigDecimal expectedRecargo = cotizacion.getSubtotal()
                .multiply(new BigDecimal("0.30"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedTotal = cotizacion.getSubtotal().add(expectedRecargo);

        assertThat(cotizacion.getRecargoUrgencia()).isEqualByComparingTo(expectedRecargo);
        assertThat(cotizacion.getTotal()).isEqualByComparingTo(expectedTotal);
    }

    /**
     * Property 10: tiempoUrgenteEsTechoMitadConMinimo
     *
     * Para cualquier lista de reparaciones con tiempoEstimadoDias >= 1
     * y cotización con esUrgente = true,
     * tiempoEstimadoDias == max(⌈max_dias / 2⌉, 1).
     *
     * Validates: Requirements 4.3, 4.4
     */
    @Property
    void tiempoUrgenteEsTechoMitadConMinimo(@ForAll("calzadoAleatorio") Calzado calzado,
                                             @ForAll("reparacionesAleatorias") List<Reparacion> reparaciones) {
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, true, new UrgentPricingStrategy());

        int maxDias = reparaciones.stream()
                .mapToInt(Reparacion::getTiempoEstimadoDias)
                .max()
                .orElse(1);
        int expectedTiempo = Math.max((int) Math.ceil(maxDias / 2.0), 1);

        assertThat(cotizacion.getTiempoEstimadoDias()).isEqualTo(expectedTiempo);
    }
}
