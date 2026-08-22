package com.tallerdae.cotizador.properties;

import com.tallerdae.cotizador.application.service.ListarReparacionesService;
import com.tallerdae.cotizador.domain.Reparacion;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryReparacionRepositoryAdapter;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: cotizador-reparacion-calzado, Property 2
 *
 * Validates: Requirements 2.2
 */
class ReparacionPropertyTest {

    /**
     * Property 2: reparacionRetornadaTieneEstructuraCompleta
     *
     * Para cualquier lista de Reparacion en el repositorio en memoria,
     * listar() retorna objetos con id no nulo, nombre no nulo, precioBase > 0
     * y tiempoEstimadoDias >= 1.
     *
     * Validates: Requirements 2.2
     */
    @Property
    void reparacionRetornadaTieneEstructuraCompleta(@ForAll("reparacionArbitraria") Reparacion reparacion) {
        assertThat(reparacion.getId()).isNotNull();
        assertThat(reparacion.getNombre()).isNotNull();
        assertThat(reparacion.getPrecioBase()).isGreaterThan(BigDecimal.ZERO);
        assertThat(reparacion.getTiempoEstimadoDias()).isGreaterThanOrEqualTo(1);
    }

    @Provide
    Arbitrary<Reparacion> reparacionArbitraria() {
        InMemoryReparacionRepositoryAdapter adapter = new InMemoryReparacionRepositoryAdapter();
        ListarReparacionesService service = new ListarReparacionesService(adapter);
        List<Reparacion> reparaciones = service.listar();
        return Arbitraries.of(reparaciones);
    }
}
