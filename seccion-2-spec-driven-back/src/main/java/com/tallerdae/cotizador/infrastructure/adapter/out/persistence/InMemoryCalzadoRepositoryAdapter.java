package com.tallerdae.cotizador.infrastructure.adapter.out.persistence;

import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.domain.Calzado;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida en memoria para el repositorio de Calzados.
 * Inicializa un catálogo de ejemplo al crearse.
 */
public class InMemoryCalzadoRepositoryAdapter implements CalzadoRepositoryPort {

    private final Map<UUID, Calzado> store = new ConcurrentHashMap<>();

    public InMemoryCalzadoRepositoryAdapter() {
        List<Calzado> catalogo = List.of(
            new Calzado(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000001"),
                        "Zapato de cuero",      new BigDecimal("1.20")),
            new Calzado(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000002"),
                        "Tenis deportivo",      new BigDecimal("1.00")),
            new Calzado(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000003"),
                        "Sandalia",             new BigDecimal("0.80")),
            new Calzado(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000004"),
                        "Bota industrial",      new BigDecimal("1.50")),
            new Calzado(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000005"),
                        "Mocasín",              new BigDecimal("1.10"))
        );
        catalogo.forEach(c -> store.put(c.getId(), c));
    }

    @Override
    public List<Calzado> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<Calzado> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
