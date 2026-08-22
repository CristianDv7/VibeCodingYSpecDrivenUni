package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.Reparacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReparacionRepositoryPort {

    List<Reparacion> findAll();

    Optional<Reparacion> findById(UUID id);

    List<Reparacion> findAllById(List<UUID> ids);
}
