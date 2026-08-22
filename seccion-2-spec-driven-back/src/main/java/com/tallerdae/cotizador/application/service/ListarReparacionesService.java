package com.tallerdae.cotizador.application.service;

import com.tallerdae.cotizador.application.port.in.ListarReparacionesUseCase;
import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.Reparacion;

import java.util.List;

public class ListarReparacionesService implements ListarReparacionesUseCase {

    private final ReparacionRepositoryPort reparacionRepositoryPort;

    public ListarReparacionesService(ReparacionRepositoryPort reparacionRepositoryPort) {
        this.reparacionRepositoryPort = reparacionRepositoryPort;
    }

    @Override
    public List<Reparacion> listar() {
        return reparacionRepositoryPort.findAll();
    }
}
