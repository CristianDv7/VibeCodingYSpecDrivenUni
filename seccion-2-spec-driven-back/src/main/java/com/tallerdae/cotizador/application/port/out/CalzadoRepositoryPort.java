package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.Calzado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalzadoRepositoryPort {

    List<Calzado> findAll();

    Optional<Calzado> findById(UUID id);
}
