package com.tallerdae.cotizador.application.port.in;

import com.tallerdae.cotizador.domain.Reparacion;

import java.util.List;

public interface ListarReparacionesUseCase {

    List<Reparacion> listar();
}
