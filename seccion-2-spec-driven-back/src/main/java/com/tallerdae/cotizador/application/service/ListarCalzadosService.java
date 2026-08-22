package com.tallerdae.cotizador.application.service;

import com.tallerdae.cotizador.application.port.in.ListarCalzadosUseCase;
import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.domain.Calzado;

import java.util.List;

public class ListarCalzadosService implements ListarCalzadosUseCase {

    private final CalzadoRepositoryPort calzadoRepositoryPort;

    public ListarCalzadosService(CalzadoRepositoryPort calzadoRepositoryPort) {
        this.calzadoRepositoryPort = calzadoRepositoryPort;
    }

    @Override
    public List<Calzado> listar() {
        return calzadoRepositoryPort.findAll();
    }
}
