package com.tallerdae.cotizador.infrastructure.config;

import com.tallerdae.cotizador.application.port.in.GenerarCotizacionUseCase;
import com.tallerdae.cotizador.application.port.in.ListarCalzadosUseCase;
import com.tallerdae.cotizador.application.port.in.ListarReparacionesUseCase;
import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.application.port.out.CotizacionRepositoryPort;
import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.application.service.GenerarCotizacionService;
import com.tallerdae.cotizador.application.service.ListarCalzadosService;
import com.tallerdae.cotizador.application.service.ListarReparacionesService;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryCalzadoRepositoryAdapter;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryCotizacionRepositoryAdapter;
import com.tallerdae.cotizador.infrastructure.adapter.out.persistence.InMemoryReparacionRepositoryAdapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CotizadorConfiguration {

    @Bean
    public CalzadoRepositoryPort calzadoRepositoryPort() {
        return new InMemoryCalzadoRepositoryAdapter();
    }

    @Bean
    public ReparacionRepositoryPort reparacionRepositoryPort() {
        return new InMemoryReparacionRepositoryAdapter();
    }

    @Bean
    public CotizacionRepositoryPort cotizacionRepositoryPort() {
        return new InMemoryCotizacionRepositoryAdapter();
    }

    @Bean
    public ListarCalzadosUseCase listarCalzadosUseCase(CalzadoRepositoryPort calzadoRepositoryPort) {
        return new ListarCalzadosService(calzadoRepositoryPort);
    }

    @Bean
    public ListarReparacionesUseCase listarReparacionesUseCase(ReparacionRepositoryPort reparacionRepositoryPort) {
        return new ListarReparacionesService(reparacionRepositoryPort);
    }

    @Bean
    public GenerarCotizacionUseCase generarCotizacionUseCase(CalzadoRepositoryPort calzadoRepositoryPort,
                                                             ReparacionRepositoryPort reparacionRepositoryPort,
                                                             CotizacionRepositoryPort cotizacionRepositoryPort) {
        return new GenerarCotizacionService(calzadoRepositoryPort, reparacionRepositoryPort, cotizacionRepositoryPort);
    }
}
