package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.Cotizacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotizacionRepositoryPort {

    Cotizacion save(Cotizacion cotizacion);

    Optional<Cotizacion> findById(UUID id);

    List<Cotizacion> findAll();
}
