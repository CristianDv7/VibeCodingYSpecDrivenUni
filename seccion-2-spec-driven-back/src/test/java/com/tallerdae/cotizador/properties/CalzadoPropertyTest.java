package com.tallerdae.cotizador.properties;

import com.tallerdae.cotizador.application.service.ListarCalzadosService;
import com.tallerdae.cotizador.domain.Calzado;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryCalzadoRepositoryAdapter;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: cotizador-reparacion-calzado, Property 1
 *
 * Validates: Requirements 1.2
 */
class CalzadoPropertyTest {

    /**
     * Property 1: calzadoRetornadoTieneEstructuraCompleta
     *
     * Para cualquier lista de Calzado en el repositorio en memoria,
     * listar() retorna objetos con id no nulo, nombre no nulo y factorComplejidad > 0.
     *
     * Validates: Requirements 1.2
     */
    @Property
    void calzadoRetornadoTieneEstructuraCompleta(@ForAll("calzadoArbitrario") Calzado calzado) {
        assertThat(calzado.getId()).isNotNull();
        assertThat(calzado.getNombre()).isNotNull();
        assertThat(calzado.getFactorComplejidad()).isGreaterThan(BigDecimal.ZERO);
    }

    @Provide
    Arbitrary<Calzado> calzadoArbitrario() {
        InMemoryCalzadoRepositoryAdapter adapter = new InMemoryCalzadoRepositoryAdapter();
        ListarCalzadosService service = new ListarCalzadosService(adapter);
        List<Calzado> calzados = service.listar();
        return Arbitraries.of(calzados);
    }
}
