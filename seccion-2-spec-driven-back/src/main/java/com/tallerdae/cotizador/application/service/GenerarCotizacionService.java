package com.tallerdae.cotizador.application.service;

import com.tallerdae.cotizador.application.port.in.GenerarCotizacionUseCase;
import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.application.port.out.CotizacionRepositoryPort;
import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.Calzado;
import com.tallerdae.cotizador.domain.CalzadoNotFoundException;
import com.tallerdae.cotizador.domain.Cotizacion;
import com.tallerdae.cotizador.domain.NonUrgentPricingStrategy;
import com.tallerdae.cotizador.domain.Reparacion;
import com.tallerdae.cotizador.domain.ReparacionNotFoundException;
import com.tallerdae.cotizador.domain.UrgencyPricingStrategy;
import com.tallerdae.cotizador.domain.UrgentPricingStrategy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GenerarCotizacionService implements GenerarCotizacionUseCase {

    private final CalzadoRepositoryPort calzadoRepositoryPort;
    private final ReparacionRepositoryPort reparacionRepositoryPort;
    private final CotizacionRepositoryPort cotizacionRepositoryPort;

    public GenerarCotizacionService(CalzadoRepositoryPort calzadoRepositoryPort,
                                    ReparacionRepositoryPort reparacionRepositoryPort,
                                    CotizacionRepositoryPort cotizacionRepositoryPort) {
        this.calzadoRepositoryPort = calzadoRepositoryPort;
        this.reparacionRepositoryPort = reparacionRepositoryPort;
        this.cotizacionRepositoryPort = cotizacionRepositoryPort;
    }

    @Override
    public Cotizacion generar(UUID calzadoId, List<UUID> reparacionIds, boolean esUrgente) {
        Calzado calzado = calzadoRepositoryPort.findById(calzadoId)
                .orElseThrow(() -> new CalzadoNotFoundException(calzadoId));

        List<Reparacion> reparaciones = reparacionRepositoryPort.findAllById(reparacionIds);

        Set<UUID> encontrados = reparaciones.stream()
                .map(Reparacion::getId)
                .collect(Collectors.toSet());

        for (UUID id : reparacionIds) {
            if (!encontrados.contains(id)) {
                throw new ReparacionNotFoundException(id);
            }
        }

        UrgencyPricingStrategy strategy = esUrgente
                ? new UrgentPricingStrategy()
                : new NonUrgentPricingStrategy();

        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, esUrgente, strategy);

        return cotizacionRepositoryPort.save(cotizacion);
    }
}
