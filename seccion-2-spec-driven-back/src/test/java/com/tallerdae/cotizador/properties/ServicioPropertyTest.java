package com.tallerdae.cotizador.properties;

import com.tallerdae.cotizador.application.service.GenerarCotizacionService;
import com.tallerdae.cotizador.domain.CalzadoNotFoundException;
import com.tallerdae.cotizador.domain.Reparacion;
import com.tallerdae.cotizador.domain.ReparacionNotFoundException;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryCalzadoRepositoryAdapter;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryCotizacionRepositoryAdapter;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryReparacionRepositoryAdapter;
import net.jqwik.api.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Feature: cotizador-reparacion-calzado, Property 8
 *
 * Pruebas de propiedad para el rechazo de calzado o reparación inexistente
 * en GenerarCotizacionService.
 *
 * Validates: Requirements 3.7, 3.8
 */
class ServicioPropertyTest {

    private final InMemoryCalzadoRepositoryAdapter calzadoRepo = new InMemoryCalzadoRepositoryAdapter();
    private final InMemoryReparacionRepositoryAdapter reparacionRepo = new InMemoryReparacionRepositoryAdapter();
    private final InMemoryCotizacionRepositoryAdapter cotizacionRepo = new InMemoryCotizacionRepositoryAdapter();
    private final GenerarCotizacionService service = new GenerarCotizacionService(calzadoRepo, reparacionRepo, cotizacionRepo);

    /**
     * Property 8a: calzadoInexistenteLanzaExcepcion
     *
     * Para cualquier UUID no registrado como calzado, el servicio lanza
     * CalzadoNotFoundException.
     *
     * Validates: Requirements 3.7
     */
    @Property
    void calzadoInexistenteLanzaExcepcion() {
        UUID idInexistente = UUID.randomUUID();
        List<UUID> reparacionIds = reparacionRepo.findAll().stream()
                .map(Reparacion::getId)
                .limit(1)
                .collect(Collectors.toList());

        assertThatThrownBy(() -> service.generar(idInexistente, reparacionIds, false))
                .isInstanceOf(CalzadoNotFoundException.class);
    }

    /**
     * Property 8b: reparacionInexistenteLanzaExcepcion
     *
     * Para cualquier lista con al menos un ID de reparación no registrado,
     * el servicio lanza ReparacionNotFoundException con el ID inválido.
     *
     * Validates: Requirements 3.8
     */
    @Property
    void reparacionInexistenteLanzaExcepcion() {
        UUID calzadoId = calzadoRepo.findAll().get(0).getId();
        List<UUID> reparacionIds = List.of(UUID.randomUUID());

        assertThatThrownBy(() -> service.generar(calzadoId, reparacionIds, false))
                .isInstanceOf(ReparacionNotFoundException.class);
    }
}
