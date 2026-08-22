package com.tallerdae.cotizador.infrastructure.adapter.out.persistence;

import com.tallerdae.cotizador.application.port.out.CotizacionRepositoryPort;
import com.tallerdae.cotizador.domain.Cotizacion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida en memoria para el repositorio de Cotizaciones.
 * Persiste las cotizaciones generadas en el ciclo de vida de la aplicación.
 */
public class InMemoryCotizacionRepositoryAdapter implements CotizacionRepositoryPort {

    private final Map<UUID, Cotizacion> store = new ConcurrentHashMap<>();

    @Override
    public Cotizacion save(Cotizacion cotizacion) {
        store.put(cotizacion.getId(), cotizacion);
        return cotizacion;
    }

    @Override
    public Optional<Cotizacion> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Cotizacion> findAll() {
        return List.copyOf(store.values());
    }
}
