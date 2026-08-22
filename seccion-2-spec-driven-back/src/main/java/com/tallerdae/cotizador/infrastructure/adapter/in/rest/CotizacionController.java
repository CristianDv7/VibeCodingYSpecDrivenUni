package com.tallerdae.cotizador.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerdae.cotizador.application.port.in.GenerarCotizacionUseCase;
import com.tallerdae.cotizador.domain.Cotizacion;

@RestController
@RequestMapping("/cotizaciones")
public class CotizacionController {

    private final GenerarCotizacionUseCase generarCotizacionUseCase;

    public CotizacionController(GenerarCotizacionUseCase generarCotizacionUseCase) {
        this.generarCotizacionUseCase = generarCotizacionUseCase;
    }

    @PostMapping
    public ResponseEntity<CotizacionResponse> generar(@RequestBody CotizacionRequest request) {
        Cotizacion cotizacion = generarCotizacionUseCase.generar(
                request.getCalzadoId(),
                request.getReparacionIds(),
                request.isEsUrgente()
        );
        CotizacionResponse response = CotizacionMapper.toResponse(cotizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
