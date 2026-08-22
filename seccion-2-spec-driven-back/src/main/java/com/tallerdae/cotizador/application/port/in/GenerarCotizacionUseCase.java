package com.tallerdae.cotizador.application.port.in;

import com.tallerdae.cotizador.domain.Cotizacion;

import java.util.List;
import java.util.UUID;

public interface GenerarCotizacionUseCase {

    Cotizacion generar(UUID calzadoId, List<UUID> reparacionIds, boolean esUrgente);
}
