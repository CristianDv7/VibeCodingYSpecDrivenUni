package com.tallerdae.cotizador.infrastructure.adapter.out.persistence;

import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.Reparacion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador de salida en memoria para el repositorio de Reparaciones.
 * Inicializa un catálogo de ejemplo al crearse.
 */
public class InMemoryReparacionRepositoryAdapter implements ReparacionRepositoryPort {

    private final Map<UUID, Reparacion> store = new ConcurrentHashMap<>();

    public InMemoryReparacionRepositoryAdapter() {
        List<Reparacion> catalogo = List.of(
            new Reparacion(UUID.fromString("b2c3d4e5-0002-0002-0002-000000000001"),
                           "Cambio de suela",          new BigDecimal("25.00"), 3),
            new Reparacion(UUID.fromString("b2c3d4e5-0002-0002-0002-000000000002"),
                           "Costura de capellada",     new BigDecimal("15.00"), 2),
            new Reparacion(UUID.fromString("b2c3d4e5-0002-0002-0002-000000000003"),
                           "Lustrado y acondicionado", new BigDecimal("10.00"), 1),
            new Reparacion(UUID.fromString("b2c3d4e5-0002-0002-0002-000000000004"),
                           "Cambio de tacón",          new BigDecimal("20.00"), 2),
            new Reparacion(UUID.fromString("b2c3d4e5-0002-0002-0002-000000000005"),
                           "Reparación de cremallera",  new BigDecimal("18.00"), 2)
        );
        catalogo.forEach(r -> store.put(r.getId(), r));
    }

    @Override
    public List<Reparacion> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Reparacion> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Reparacion> findAllById(List<UUID> ids) {
        return ids.stream()
                  .map(store::get)
                  .filter(r -> r != null)
                  .collect(Collectors.toList());
    }
}
