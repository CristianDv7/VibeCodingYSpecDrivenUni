package com.tallerdae.cotizador.application.port.in;

import java.util.List;

import com.tallerdae.cotizador.domain.Calzado;

public interface ListarCalzadosUseCase {

    List<Calzado> listar();
}
